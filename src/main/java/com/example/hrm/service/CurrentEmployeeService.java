package com.example.hrm.service;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentEmployeeService {

    private final UserAccountRepository userRepo;
    private final EmployeeRepository employeeRepo;

    public Employee requireEmployee(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("Not authenticated");
        }

        String username = principal.getName();

        UserAccount u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return employeeRepo.findByUserId(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found for userId: " + u.getId()));
    }

    public Integer requireCurrentEmpId(Principal principal) {
        return requireEmployee(principal).getId();
    }
}
