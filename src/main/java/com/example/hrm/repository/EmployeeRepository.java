package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByUserId(Integer userId);
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);
}
