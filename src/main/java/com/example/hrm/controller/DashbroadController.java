package com.example.hrm.controller;

import com.example.hrm.entity.Contract;
import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

import com.example.hrm.repository.HolidayRepository;
import com.example.hrm.entity.Holiday;

@Controller
public class DashbroadController {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final com.example.hrm.repository.AttendanceLogRepository attendanceLogRepository;
    private final com.example.hrm.repository.UserRepository userRepository;
    private final HolidayRepository holidayRepository;

    public DashbroadController(EmployeeRepository employeeRepository,
                               ContractRepository contractRepository,
                               com.example.hrm.repository.AttendanceLogRepository attendanceLogRepository,
                               com.example.hrm.repository.UserRepository userRepository,
                               HolidayRepository holidayRepository) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isHr = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isAdmin) return "redirect:/dashboard/admin";
        if (isHr) return "redirect:/dashboard/hr";
        if (isManager) return "redirect:/dashboard/manager";
        if (isEmployee) return "redirect:/dashboard/employee";

        return "redirect:/login";
    }

    @GetMapping("/dashboard/admin")
    public String adminDash(Model model) {
        model.addAttribute("sidebar", "sidebar-admin.html");
        return "auth/AdminDashbroad";
    }

    @GetMapping("/dashboard/hr")
    public String hrDash(Model model) {

        long totalEmployees = employeeRepository.count();

        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);

        long expiringContracts = contractRepository.countExpiringActiveContracts(today, next30Days);

        long active = employeeRepository.countByStatus("ACTIVE");
        long inactive = employeeRepository.countByStatus("INACTIVE");
        long resigned = employeeRepository.countByStatus("RESIGNED");

        List<Contract> expiringContractList = contractRepository.findExpiringActiveContracts(today, next30Days);

        model.addAttribute("sidebar", "sidebar-hr.html");
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("expiringContracts", expiringContracts);
        model.addAttribute("active", active);
        model.addAttribute("inactive", inactive);
        model.addAttribute("resigned", resigned);
        model.addAttribute("expiringContractList", expiringContractList);

        return "auth/HrDashbroad";
    }

    @GetMapping("/dashboard/manager")
    public String managerDash(Model model) {
        model.addAttribute("sidebar", "sidebar-manager.html");
        return "auth/ManagerDashbroad";
    }

    @GetMapping("/dashboard/employee")
    public String employeeDash(Model model, Authentication auth) {
        model.addAttribute("sidebar", "sidebar-employee.html");

        if (auth != null) {
            String username = auth.getName();
            com.example.hrm.entity.User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                com.example.hrm.entity.Employee emp = employeeRepository.findByUserId(user.getUserId()).orElse(null);
                if (emp != null) {
                    List<com.example.hrm.entity.AttendanceLog> logs = attendanceLogRepository.findByEmployee_EmpIdOrderByWorkDateDesc(emp.getEmpId());
                    long workedMinsToday = 0;
                    long overtimeMinsToday = 0;
                    long workedMinsWeek = 0;
                    long overtimeMinsWeek = 0;
                    long workedMinsMonth = 0;
                    long overtimeMinsMonth = 0;
                    
                    int[] weekDailyMinutes = new int[7];

                    LocalDate today = LocalDate.now();
                    LocalDate startOfMonth = today.withDayOfMonth(1);
                    
                    java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
                    int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
                    int currentYear = today.getYear();

                    com.example.hrm.entity.AttendanceLog logToday = null;
                    for (com.example.hrm.entity.AttendanceLog log : logs) {
                        if (log.getWorkDate().isEqual(today)) {
                            if (logToday == null || (log.getCheckIn() != null && logToday.getCheckIn() != null && log.getCheckIn().isAfter(logToday.getCheckIn()))) {
                                logToday = log;
                            }
                        }
                        
                        if (log.getCheckIn() != null && log.getCheckOut() != null) {
                            long dur = java.time.Duration.between(log.getCheckIn(), log.getCheckOut()).toMinutes();
                            dur = Math.max(0, dur);
                            long ot = 0;

                            if (log.getScheduledEndAt() != null && log.getCheckOut().isAfter(log.getScheduledEndAt())) {
                                ot = java.time.Duration.between(log.getScheduledEndAt(), log.getCheckOut()).toMinutes();
                                ot = Math.max(0, ot);
                            }
                            
                            // Today Check
                            if (log.getWorkDate().isEqual(today)) {
                                workedMinsToday += dur;
                                overtimeMinsToday += ot;
                            }
                            
                            // Week Check
                            if (log.getWorkDate().get(weekFields.weekOfWeekBasedYear()) == currentWeek && log.getWorkDate().getYear() == currentYear) {
                                workedMinsWeek += dur;
                                overtimeMinsWeek += ot;
                                
                                int dayOfWeek = log.getWorkDate().getDayOfWeek().getValue();
                                int index = (dayOfWeek == 7) ? 0 : dayOfWeek; // 0=Sunday, 1=Monday ... 6=Saturday
                                weekDailyMinutes[index] += dur;
                            }
                            
                            // Month Check
                            if (log.getWorkDate().isAfter(startOfMonth.minusDays(1))) {
                                workedMinsMonth += dur;
                                overtimeMinsMonth += ot;
                            }
                        }
                    }

                    int maxDailyMins = 1;
                    for (int m : weekDailyMinutes) {
                        if (m > maxDailyMins) maxDailyMins = m;
                    }

                    model.addAttribute("workedMinsToday", workedMinsToday);
                    model.addAttribute("otMinsToday", overtimeMinsToday);
                    model.addAttribute("workedTodayStr", (workedMinsToday / 60) + "h " + (workedMinsToday % 60) + "m");
                    model.addAttribute("otTodayStr", (overtimeMinsToday / 60) + "h " + (overtimeMinsToday % 60) + "m");
                    model.addAttribute("workedWeekStr", (workedMinsWeek / 60) + "h " + (workedMinsWeek % 60) + "m");
                    model.addAttribute("otWeekStr", (overtimeMinsWeek / 60) + "h " + (overtimeMinsWeek % 60) + "m");
                    model.addAttribute("workedMonthStr", (workedMinsMonth / 60) + "h " + (workedMinsMonth % 60) + "m");
                    model.addAttribute("otMonthStr", (overtimeMinsMonth / 60) + "h " + (overtimeMinsMonth % 60) + "m");
                    
                    model.addAttribute("weekDailyMinutes", weekDailyMinutes);
                    model.addAttribute("maxDailyMins", maxDailyMins);
                    model.addAttribute("logToday", logToday);
                    model.addAttribute("e", emp);
                    
                    List<Holiday> upcomingHolidays = holidayRepository.findUpcomingActiveHolidays();
                    model.addAttribute("upcomingHolidays", upcomingHolidays);
                }
            }
        }
        return "auth/EmployeeDashbroad";
    }
}