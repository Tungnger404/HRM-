package com.example.hrm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashbroadController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isHr = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isAdmin) return "redirect:/dashboard/admin";
        if (isHr) return "redirect:/dashboard/hr";
        if (isManager) return "redirect:/dashboard/manager";
        if (isEmployee) return "redirect:/dashboard/employee";

        return "redirect:/login";
    }

    @GetMapping("/dashboard/admin")
    public String adminDash() {
        return "auth/AdminDashbroad";
    }

    @GetMapping("/dashboard/hr")
    public String hrDash() {
        return "auth/HrDashbroad";
    }

    @GetMapping("/dashboard/manager")
    public String managerDash() {
        return "auth/ManagerDashbroad";
    }

    @GetMapping("/dashboard/employee")
    public String employeeDash() {
        return "auth/EmployeeDashbroad";
    }
}
