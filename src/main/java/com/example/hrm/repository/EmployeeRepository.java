package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);
}
