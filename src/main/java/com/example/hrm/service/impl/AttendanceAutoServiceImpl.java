package com.example.hrm.service.impl;

import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.ShiftAssignment;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.ShiftAssignmentRepository;
import com.example.hrm.service.AttendanceAutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceAutoServiceImpl implements AttendanceAutoService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    @Override
    @Scheduled(cron = "0 0 23 * * MON-FRI")
    public void markAbsentForToday() {
        LocalDate today = LocalDate.now();

        DayOfWeek day = today.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return;
        }

        List<ShiftAssignment> workAssignments =
                shiftAssignmentRepository.findByWorkDateAndAssignmentType(today, "WORK");

        for (ShiftAssignment assignment : workAssignments) {
            Integer empId = assignment.getEmployee().getEmpId();

            boolean exists = attendanceLogRepository
                    .findByEmployee_EmpIdAndWorkDate(empId, today)
                    .isPresent();

            if (!exists) {
                AttendanceLog log = AttendanceLog.builder()
                        .employee(assignment.getEmployee())
                        .workDate(today)
                        .checkIn(null)
                        .checkOut(null)
                        .status("ABSENT")
                        .workType("WORK")
                        .assignmentId(assignment.getAssignmentId())
                        .shiftId(assignment.getShiftTemplate() != null ? assignment.getShiftTemplate().getShiftId() : null)
                        .isLate(false)
                        .lateMinutes(0)
                        .isEarlyLeave(false)
                        .earlyLeaveMinutes(0)
                        .build();

                attendanceLogRepository.save(log);
            }
        }
    }
}