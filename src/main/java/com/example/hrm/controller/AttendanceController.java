package com.example.hrm.controller;
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

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String attendancePage(Model model) {

        Integer empId = attendanceService.getEmpIdFromSecurity();

        model.addAttribute("logs", attendanceService.getHistory(empId));

        return "employee/attendance";
    }

    @PostMapping("/check-in")
    public String checkIn(RedirectAttributes ra) {

        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkIn(empId);
            ra.addFlashAttribute("message", "Check-in successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employee/attendance";
    }

    @PostMapping("/check-out")
    public String checkOut( RedirectAttributes ra) {
        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkOut(empId);
            ra.addFlashAttribute("message", "Check-out successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employee/attendance";

    }
}
