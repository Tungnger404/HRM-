package com.example.hrm.service;

import com.example.hrm.entity.Department;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerDepartmentAccessService {

    private final CurrentEmployeeService currentEmployeeService;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public Integer currentManagerEmpId(Principal principal) {
        return currentEmployeeService.requireCurrentEmpId(principal);
    }

    public Employee currentManagerEmployee(Principal principal) {
        Integer managerEmpId = currentManagerEmpId(principal);
        return employeeRepository.findById(managerEmpId)
                .orElseThrow(() -> new IllegalArgumentException("Manager employee not found: " + managerEmpId));
    }

    public Integer currentManagerDeptId(Principal principal) {
        Employee manager = currentManagerEmployee(principal);
        if (manager.getDeptId() == null) {
            throw new AccessDeniedException("Manager chưa được gán phòng ban.");
        }
        return manager.getDeptId();
    }

    public List<Department> getManagedDepartments(Principal principal) {
        Integer deptId = currentManagerDeptId(principal);
        return departmentRepository.findById(deptId)
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    public boolean canManageDepartment(Integer deptId, Principal principal) {
        if (deptId == null) return false;
        Integer managerDeptId = currentManagerDeptId(principal);
        return deptId.equals(managerDeptId);
    }

    public boolean canManageEmployee(Integer empId, Principal principal) {
        if (empId == null) return false;
        Integer managerDeptId = currentManagerDeptId(principal);
        return employeeRepository.existsEmployeeInSameDepartment(managerDeptId, empId);
    }

    public void requireManagedEmployee(Integer empId, Principal principal) {
        if (!canManageEmployee(empId, principal)) {
            throw new AccessDeniedException("Bạn không có quyền quản lý nhân viên này.");
        }
    }

    public Employee requireManagedEmployeeEntity(Integer empId, Principal principal) {
        requireManagedEmployee(empId, principal);
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));
    }
}