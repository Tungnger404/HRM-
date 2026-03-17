package com.example.hrm.service;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.Employee;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {
    List<Employee> list(String q, String status);
    Employee getById(Integer id);
    List<Employee> listManagedByDepartment(Integer managerDeptId, String q, String status);
    Employee create(EmployeeAdd form);
    Employee update(EmployeeAdd form);
    void delete(Integer id);

    EmployeeAdd toForm(Employee e);

    void createEmployeeFromCandidate(Integer candidateId,
                                     LocalDate joinDate,
                                     Integer jobId,
                                     Integer deptId,
                                     String identityCard,
                                     String taxCode);
}
