package com.example.hrm.service;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> list(String q);
    Employee getById(Integer id);

    Employee create(EmployeeAdd form);
    Employee update(EmployeeAdd form);
    void delete(Integer id);

    EmployeeAdd toForm(Employee e);
}
