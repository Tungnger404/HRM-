package com.example.hrm.service;

import com.example.hrm.entity.Employee;

import java.security.Principal;

public interface CurrentEmployeeService {
    Employee requireEmployee(Principal principal);
    Integer requireCurrentEmpId(Principal principal);
    Integer requireUserId(Principal principal);
}