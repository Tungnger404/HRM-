package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;
import java.util.Optional;

@ControllerAdvice
public class GlobalUserInfoAdvice {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public GlobalUserInfoAdvice(UserRepository userRepository,
                                EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @ModelAttribute
    public void addCurrentUserInfo(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            model.addAttribute("currentAvatarPath", null);
            model.addAttribute("currentDisplayName", "User");
            model.addAttribute("currentAvatarInitial", "U");
            model.addAttribute("currentRoleName", "Account");
            return;
        }

        String principal = authentication.getName();

        Optional<User> userOpt = userRepository.findByUsername(principal);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(principal);
        }

        String displayName = principal;
        String avatarPath = null;

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            avatarPath = user.getAvatarPath();

            displayName = user.getUsername();

            try {
                Employee employee = employeeRepository.findByUserId(user.getUserId()).orElse(null);
                if (employee != null && employee.getFullName() != null && !employee.getFullName().isBlank()) {
                    displayName = employee.getFullName();
                }
            } catch (Exception ignored) {
            }
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = "User";
        }

        String roleName = extractRoleName(authentication.getAuthorities());

        model.addAttribute("currentAvatarPath", avatarPath);
        model.addAttribute("currentDisplayName", displayName);
        model.addAttribute("currentAvatarInitial", displayName.substring(0, 1).toUpperCase());
        model.addAttribute("currentRoleName", roleName);
    }

    private String extractRoleName(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return "Account";
        }

        for (GrantedAuthority authority : authorities) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }

            String value = authority.getAuthority().trim();
            if (value.startsWith("ROLE_")) {
                return value.substring(5);
            }
            return value;
        }

        return "Account";
    }
}