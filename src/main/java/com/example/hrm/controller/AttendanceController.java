package com.example.hrm.controller;
import com.example.hrm.service.AttendanceAutoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.hrm.service.AttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employee/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceAutoService attendanceAutoService;
    private final com.example.hrm.repository.EmployeeRepository employeeRepository;
    private final com.example.hrm.repository.UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceAutoService attendanceAutoService,
                                com.example.hrm.repository.EmployeeRepository employeeRepository,
                                com.example.hrm.repository.UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.attendanceAutoService = attendanceAutoService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }
    @GetMapping
    public String attendancePage(Model model, Authentication auth) {

        Integer empId = attendanceService.getEmpIdFromSecurity();
        java.util.List<com.example.hrm.entity.AttendanceLog> logs = attendanceService.getHistory(empId);
        model.addAttribute("logs", logs);

        if (auth != null) {
            String username = auth.getName();
            com.example.hrm.entity.User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                com.example.hrm.entity.Employee emp = employeeRepository.findByUserId(user.getUserId()).orElse(null);
                model.addAttribute("u", user);
                model.addAttribute("e", emp);
            }
        }

        long hoursToday = 0;
        long hoursWeek = 0;
        long hoursMonth = 0;
        long overtimeMonth = 0;
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());

        for (com.example.hrm.entity.AttendanceLog log : logs) {
            if (log.getCheckIn() != null && log.getCheckOut() != null) {
                long dur = java.time.Duration.between(log.getCheckIn(), log.getCheckOut()).toMinutes();
                long ot = 0;
                if (log.getScheduledEndAt() != null && log.getCheckOut().isAfter(log.getScheduledEndAt())) {
                    ot = java.time.Duration.between(log.getScheduledEndAt(), log.getCheckOut()).toMinutes();
                }
                
                if (log.getWorkDate().isEqual(today)) {
                    hoursToday += dur;
                }
                if (log.getWorkDate().get(weekFields.weekOfWeekBasedYear()) == currentWeek && log.getWorkDate().getYear() == today.getYear()) {
                    hoursWeek += dur;
                }
                if (log.getWorkDate().getMonth() == today.getMonth() && log.getWorkDate().getYear() == today.getYear()) {
                    hoursMonth += dur;
                    overtimeMonth += ot;
                }
            }
        }
        
        model.addAttribute("hrsToday", String.format("%.2f", hoursToday / 60.0));
        model.addAttribute("hrsWeek", String.format("%.2f", hoursWeek / 60.0));
        model.addAttribute("hrsMonth", String.format("%.2f", hoursMonth / 60.0));
        model.addAttribute("otMonth", String.format("%.2f", overtimeMonth / 60.0));
        
        return "employee/attendance";
    }

    @PostMapping("/check-in")
    public String checkIn(RedirectAttributes ra, jakarta.servlet.http.HttpServletRequest request) {

        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkIn(empId);
            ra.addFlashAttribute("message", "Check-in successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/employee/attendance");
    }

    @PostMapping("/check-out")
    public String checkOut( RedirectAttributes ra, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkOut(empId);
            ra.addFlashAttribute("message", "Check-out successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/employee/attendance");

    }
    @PostMapping("/dev/mark-absent-now")
    public String markAbsentNow(RedirectAttributes ra) {
        try {
            attendanceAutoService.markAbsentForToday();
            ra.addFlashAttribute("message", "Marked absent job executed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/attendance";
    }
}
