package com.example.hrm.controller;

import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/hr/attendance-records")
public class HrAttendanceRecordController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    public HrAttendanceRecordController(EmployeeRepository employeeRepository,
                                        AttendanceLogRepository attendanceLogRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceLogRepository = attendanceLogRepository;
    }

    @GetMapping
    public String viewMonthlyRecords(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        
        LocalDate today = LocalDate.now();
        int y = (year != null) ? year : today.getYear();
        int m = (month != null) ? month : today.getMonthValue();
        YearMonth yearMonth = YearMonth.of(y, m);
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        if (endDate.isAfter(today)) {
            // Optional: limit to today if you don't want to show future as absent
        }

        // Generate list of days
        List<LocalDate> daysInMonth = Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(yearMonth.lengthOfMonth())
                .collect(Collectors.toList());

        // 1. Get all active and probation employees
        List<Employee> activeEmployees = employeeRepository.search(null, "ACTIVE");
        List<Employee> probationEmployees = employeeRepository.search(null, "PROBATION");
        activeEmployees.addAll(probationEmployees);
        activeEmployees = activeEmployees.stream().distinct().collect(Collectors.toList());

        // 2. Fetch logs for the whole month
        List<MonthlyAttendanceRecord> records = activeEmployees.stream().map(emp -> {
            List<AttendanceLog> logs = attendanceLogRepository
                    .findByEmployee_EmpIdAndWorkDateBetweenOrderByWorkDateAsc(emp.getEmpId(), startDate, endDate);
            
            Map<LocalDate, AttendanceLog> logMap = logs.stream()
                    .collect(Collectors.toMap(log -> log.getWorkDate(), log -> log));
            
            // Calculate total hours
            double totalHrs = 0;
            for (AttendanceLog log : logs) {
                if (log.getCheckIn() != null && log.getCheckOut() != null) {
                    totalHrs += Duration.between(log.getCheckIn(), log.getCheckOut()).toMinutes() / 60.0;
                }
            }
            
            return new MonthlyAttendanceRecord(emp, logMap, String.format("%.1f", totalHrs));
        }).collect(Collectors.toList());

        model.addAttribute("records", records);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("currentYearMonth", yearMonth);
        model.addAttribute("today", today);
        
        return "hr/attendance-records";
    }

    @Data
    @AllArgsConstructor
    public static class MonthlyAttendanceRecord {
        private Employee employee;
        private Map<LocalDate, AttendanceLog> logs;
        private String totalHours;
        
        public String getStatusForDate(LocalDate date, LocalDate today) {
            AttendanceLog log = logs.get(date);
            if (date.isAfter(today)) {
                return "FUTURE";
            }
            
            // Checked in
            if (log != null && log.getCheckIn() != null) {
                // If overtime or late, we could return PURPLE. Let's return BLUE for now.
                return "PRESENT"; 
            }
            
            // Weekend -> Grey
            java.time.DayOfWeek dow = date.getDayOfWeek();
            if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
                return "OFF"; // Xám đen
            }
            
            // Weekday, no check in & past/today
            return "ABSENT"; // Đỏ
        }
        
        public String getTooltipForDate(LocalDate date) {
            AttendanceLog log = logs.get(date);
            if (log != null && log.getCheckIn() != null) {
               String in = log.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
               String out = log.getCheckOut() != null ? log.getCheckOut().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "--:--";
               return "In: " + in + " - Out: " + out;
            }
            return "No Check In";
        }
    }
}
