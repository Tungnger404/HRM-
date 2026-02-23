package com.example.hrm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profileRouter(Authentication auth) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isHr = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isHr) return "redirect:/hr/profile";
        if (isAdmin) return "redirect:/admin/profile";
        if (isManager) return "redirect:/manager/profile";
        if (isEmployee) return "redirect:/employee/profile";

        return "redirect:/login";
    }
}
