package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminProfileController {

    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;

    public AdminProfileController(UserRepository userRepo, EmployeeRepository employeeRepo) {
        this.userRepo = userRepo;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        String principal = (auth == null) ? null : auth.getName();

        if (principal == null || principal.isBlank()) {
            model.addAttribute("err", "Not authenticated");
            return "admin/profile";
        }

        Optional<User> uOpt = userRepo.findByUsername(principal);
        if (uOpt.isEmpty()) uOpt = userRepo.findByEmail(principal);

        if (uOpt.isEmpty()) {
            model.addAttribute("err", "User not found for: " + principal);
            return "admin/profile";
        }

        User u = uOpt.get();
        model.addAttribute("u", u);

        Employee e = employeeRepo.findByUserId(u.getUserId()).orElse(null);
        model.addAttribute("e", e);

        return "admin/profile"; // templates/admin/profile.html
    }
}
