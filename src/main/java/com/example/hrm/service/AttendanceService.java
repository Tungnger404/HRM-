package com.example.hrm.service;

import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftAttendanceRuleRepository shiftAttendanceRuleRepository;

    public AttendanceService(AttendanceLogRepository attendanceLogRepository,
                             EmployeeRepository employeeRepository,
                             UserRepository userRepository,
                             ShiftAssignmentRepository shiftAssignmentRepository,
                             ShiftAttendanceRuleRepository shiftAttendanceRuleRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.shiftAttendanceRuleRepository = shiftAttendanceRuleRepository;
    }

    public void checkIn(Integer empId) {
        LocalDate today = LocalDate.now();

        if (attendanceLogRepository.findByEmployee_EmpIdAndWorkDate(empId, today).isPresent()) {
            throw new RuntimeException("You have already checked in today.");
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        ShiftAssignment assignment = shiftAssignmentRepository.findByEmployee_EmpIdAndWorkDate(empId, today)
                .orElseThrow(() -> new RuntimeException("No shift assignment for today."));

        if (!"WORK".equalsIgnoreCase(assignment.getAssignmentType())) {
            throw new RuntimeException("Today is not a working shift (" + assignment.getAssignmentType() + ").");
        }

        ShiftTemplate shift = assignment.getShiftTemplate();
        if (shift == null) {
            throw new RuntimeException("Shift is missing in assignment.");
        }

        ShiftAttendanceRule rule = shiftAttendanceRuleRepository
                .findByShiftTemplate_ShiftIdAndIsActiveTrue(shift.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift rule not found for " + shift.getShiftCode()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = LocalDateTime.of(today, shift.getStartTime());
        LocalDateTime scheduledEnd = LocalDateTime.of(today, shift.getEndTime());

        if (Boolean.TRUE.equals(shift.getIsOvernight()) || shift.getEndTime().isBefore(shift.getStartTime())) {
            scheduledEnd = scheduledEnd.plusDays(1);
        }

        LocalDateTime earliestCheckIn = scheduledStart.minusMinutes(rule.getEarlyCheckinMinutes());
        if (now.isBefore(earliestCheckIn)) {
            throw new RuntimeException("Too early to check in.");
        }

        int lateMinutes = (int) Math.max(0, Duration.between(scheduledStart, now).toMinutes());
        boolean isLate = lateMinutes > rule.getLateThresholdMinutes();

        AttendanceLog log = AttendanceLog.builder()
                .employee(employee)
                .workDate(today)
                .checkIn(now)
                .status(isLate ? "LATE" : "ON_TIME")
                .workType("WORK")
                .assignmentId(assignment.getAssignmentId())
                .shiftId(shift.getShiftId())
                .scheduledStartAt(scheduledStart)
                .scheduledEndAt(scheduledEnd)
                .isLate(isLate)
                .lateMinutes(lateMinutes)
                .isEarlyLeave(false)
                .earlyLeaveMinutes(0)
                .build();

        attendanceLogRepository.save(log);
    }

    public void checkOut(Integer empId) {
        AttendanceLog log = attendanceLogRepository
                .findTopByEmployee_EmpIdAndCheckOutIsNullOrderByWorkDateDesc(empId)
                .orElseThrow(() -> new RuntimeException("You have not checked in."));

        if ("ABSENT".equals(log.getStatus())) {
            throw new RuntimeException("You are marked absent today, cannot check out.");
        }

        LocalDateTime now = LocalDateTime.now();
        log.setCheckOut(now);

        boolean earlyLeave = false;
        int earlyLeaveMinutes = 0;

        if (log.getScheduledEndAt() != null && now.isBefore(log.getScheduledEndAt())) {
            earlyLeave = true;
            earlyLeaveMinutes = (int) Duration.between(now, log.getScheduledEndAt()).toMinutes();
        }

        log.setIsEarlyLeave(earlyLeave);
        log.setEarlyLeaveMinutes(earlyLeaveMinutes);

        if (earlyLeave) {
            if ("LATE".equals(log.getStatus())) {
                log.setStatus("LATE_EARLY_LEAVE");
            } else {
                log.setStatus("EARLY_LEAVE");
            }
        }

        attendanceLogRepository.save(log);
    }

    public Integer getEmpIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }

    public List<AttendanceLog> getHistory(Integer empId) {
        return attendanceLogRepository.findByEmployee_EmpIdOrderByWorkDateDesc(empId);
    }

    public Integer getEmpIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }
}