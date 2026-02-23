package com.example.hrm.repository;

import com.example.hrm.entity.EmployeeChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeChangeRequestRepository extends JpaRepository<EmployeeChangeRequest, Integer> {

    List<EmployeeChangeRequest> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    List<EmployeeChangeRequest> findByStatusOrderByCreatedAtDesc(String status);
}
