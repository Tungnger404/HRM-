package com.example.hrm.service.impl;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserAccountRepository;
import com.example.hrm.service.CurrentEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentEmployeeServiceImpl implements CurrentEmployeeService {

    private final UserAccountRepository userRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    public Employee requireEmployee(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = principal.getName();

        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + username));

        return employeeRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Employee autoEmp = Employee.builder()
                            .fullName(user.getUsername() + " (System)")
                            .userId(user.getId())
                            .status("ACTIVE")
                            .includeInPayroll(false)
                            .build();
                    return employeeRepo.save(autoEmp);
                });
    }

    @Override
    public Integer requireCurrentEmpId(Principal principal) {
        return requireEmployee(principal).getEmpId();
    }

    @Override
    public Integer requireUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = principal.getName();

        return userRepo.findByUsername(username)
                .map(UserAccount::getId)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + username));
    }
}