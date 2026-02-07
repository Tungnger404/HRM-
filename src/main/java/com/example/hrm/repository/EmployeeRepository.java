package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    List<Employee> findByFullNameContainingIgnoreCase(String keyword);

    Optional<Employee> findByUserId(Integer userId);

    long countByStatusIgnoreCase(String status);

    long countByJoinDateAfter(LocalDate date);
}
