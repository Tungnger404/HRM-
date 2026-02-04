package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.RecruitmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitmentRequestRepository
        extends JpaRepository<RecruitmentRequest, Integer> {

    List<RecruitmentRequest> findByCreatedBy(Employee employee);
}
