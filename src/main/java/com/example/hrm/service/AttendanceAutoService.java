package com.example.hrm.service;
import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceAutoService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    @Scheduled(cron = "0 0 23 * * MON-FRI")
    public void markAbsentForToday() {

        LocalDate today = LocalDate.now();

        DayOfWeek day = today.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return;
        }

        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            boolean exists = attendanceLogRepository
                    .findByEmployee_EmpIdAndWorkDate(employee.getEmpId(), today)
                    .isPresent();

            if (!exists) {
                AttendanceLog log = AttendanceLog.builder()
                        .employee(employee)
                        .workDate(today)
                        .checkIn(null)
                        .checkOut(null)
                        .status("ABSENT")
                        .workType("NORMAL")
                        .build();

                attendanceLogRepository.save(log);
            }
        }
    }
}