package com.example.hrm.service;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentEmployeeService {

    private final UserAccountRepository userRepo;
    private final EmployeeRepository employeeRepo;

    public Employee requireEmployee(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = principal.getName();

        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + username));

        return employeeRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Employee not found for userId: " + user.getId()));
    }

    public Integer requireCurrentEmpId(Principal principal) {
        return requireEmployee(principal).getId();
    }
}
