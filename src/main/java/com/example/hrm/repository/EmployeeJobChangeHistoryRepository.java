package com.example.hrm.repository;

import com.example.hrm.entity.EmployeeJobChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeJobChangeHistoryRepository
        extends JpaRepository<EmployeeJobChangeHistory, Integer> {

    List<EmployeeJobChangeHistory> findByEmpIdOrderByChangeDateDesc(Integer empId);
}