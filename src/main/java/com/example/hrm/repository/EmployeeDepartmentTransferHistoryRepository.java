package com.example.hrm.repository;

import com.example.hrm.entity.EmployeeDepartmentTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeDepartmentTransferHistoryRepository
        extends JpaRepository<EmployeeDepartmentTransferHistory, Integer> {

    List<EmployeeDepartmentTransferHistory> findByEmpIdOrderByTransferDateDesc(Integer empId);
}