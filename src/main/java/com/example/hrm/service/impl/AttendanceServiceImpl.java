package com.example.hrm.service.impl;

import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.ShiftAssignment;
import com.example.hrm.entity.ShiftAttendanceRule;
import com.example.hrm.entity.ShiftTemplate;
import com.example.hrm.entity.User;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.ShiftAssignmentRepository;
import com.example.hrm.repository.ShiftAttendanceRuleRepository;
import com.example.hrm.repository.ShiftTemplateRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.AttendanceService;
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
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftAttendanceRuleRepository shiftAttendanceRuleRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    public AttendanceServiceImpl(AttendanceLogRepository attendanceLogRepository,
                                 EmployeeRepository employeeRepository,
                                 UserRepository userRepository,
                                 ShiftAssignmentRepository shiftAssignmentRepository,
                                 ShiftAttendanceRuleRepository shiftAttendanceRuleRepository,
                                 ShiftTemplateRepository shiftTemplateRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.shiftAttendanceRuleRepository = shiftAttendanceRuleRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
    }

    @Override
    public void checkIn(Integer empId) {
        LocalDate today = LocalDate.now();

        // if (attendanceLogRepository.findByEmployee_EmpIdAndWorkDate(empId, today).isPresent()) {
        //     throw new RuntimeException("You have already checked in today.");
        // }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();

        ShiftAssignment assignment = shiftAssignmentRepository.findByEmployee_EmpIdAndWorkDate(empId, today).orElse(null);

        Long assignmentId = null;
        Integer shiftId = null;
        LocalDateTime scheduledStart = null;
        LocalDateTime scheduledEnd = null;
        boolean isLate = false;
        int lateMinutes = 0;

        if (assignment != null && "WORK".equalsIgnoreCase(assignment.getAssignmentType()) && assignment.getShiftTemplate() != null) {
            ShiftTemplate shift = assignment.getShiftTemplate();
            assignmentId = assignment.getAssignmentId();
            shiftId = shift.getShiftId();

            scheduledStart = LocalDateTime.of(today, shift.getStartTime());
            scheduledEnd = LocalDateTime.of(today, shift.getEndTime());

            if (Boolean.TRUE.equals(shift.getIsOvernight()) || shift.getEndTime().isBefore(shift.getStartTime())) {
                scheduledEnd = scheduledEnd.plusDays(1);
            }

            if (now.isAfter(scheduledStart)) {
                lateMinutes = (int) Math.max(0, Duration.between(scheduledStart, now).toMinutes());
                ShiftAttendanceRule rule = shiftAttendanceRuleRepository
                        .findByShiftTemplate_ShiftIdAndIsActiveTrue(shift.getShiftId())
                        .orElse(null);
                if (rule != null) {
                    isLate = lateMinutes > rule.getLateThresholdMinutes();
                } else {
                    isLate = lateMinutes > 0;
                }
            }
        } else {
            ShiftTemplate defaultShift = shiftTemplateRepository.findByShiftCode("DAY").orElseGet(() ->
                    shiftTemplateRepository.findAll().stream()
                            .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                            .findFirst()
                            .orElse(null)
            );

            if (defaultShift != null) {
                scheduledStart = LocalDateTime.of(today, defaultShift.getStartTime());
                scheduledEnd = LocalDateTime.of(today, defaultShift.getEndTime());
                shiftId = defaultShift.getShiftId();

                if (Boolean.TRUE.equals(defaultShift.getIsOvernight()) || defaultShift.getEndTime().isBefore(defaultShift.getStartTime())) {
                    scheduledEnd = scheduledEnd.plusDays(1);
                }

                if (now.isAfter(scheduledStart)) {
                    lateMinutes = (int) Math.max(0, Duration.between(scheduledStart, now).toMinutes());
                    ShiftAttendanceRule rule = shiftAttendanceRuleRepository
                            .findByShiftTemplate_ShiftIdAndIsActiveTrue(defaultShift.getShiftId())
                            .orElse(null);
                    if (rule != null) {
                        isLate = lateMinutes > rule.getLateThresholdMinutes();
                    } else {
                        isLate = lateMinutes > 0;
                    }
                }
            }
        }

        AttendanceLog log = AttendanceLog.builder()
                .employee(employee)
                .workDate(today)
                .checkIn(now)
                .status(isLate ? "LATE" : "ON_TIME")
                .workType("WORK")
                .assignmentId(assignmentId)
                .shiftId(shiftId)
                .scheduledStartAt(scheduledStart)
                .scheduledEndAt(scheduledEnd)
                .isLate(isLate)
                .lateMinutes(lateMinutes)
                .isEarlyLeave(false)
                .earlyLeaveMinutes(0)
                .build();

        attendanceLogRepository.save(log);
    }

    @Override
    public void checkOut(Integer empId) {
        AttendanceLog log = attendanceLogRepository
                .findTopByEmployee_EmpIdAndCheckOutIsNullOrderByWorkDateDesc(empId)
                .orElseThrow(() -> new RuntimeException("You have not checked in."));

        if ("ABSENT".equals(log.getStatus())) {
            throw new RuntimeException("You are marked absent today, cannot check out.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (log.getCheckIn() != null) {
            long minsElapsed = Duration.between(log.getCheckIn(), now).toMinutes();
            if (minsElapsed < 5) {
                throw new RuntimeException("You cannot check out within 5 minutes of checking in.");
            }
        }

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

    @Override
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

    @Override
    public List<AttendanceLog> getHistory(Integer empId) {
        return attendanceLogRepository.findByEmployee_EmpIdOrderByWorkDateDesc(empId);
    }

    @Override
    public Integer getEmpIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }
}