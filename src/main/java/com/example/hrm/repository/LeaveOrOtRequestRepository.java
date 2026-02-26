package com.example.hrm.repository;

import com.example.hrm.entity.LeaveOrOtRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveOrOtRequestRepository
        extends JpaRepository<LeaveOrOtRequest, Integer> {

}