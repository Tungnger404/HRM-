package com.example.hrm.repository;

import com.example.hrm.entity.LeaveOrOtRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<LeaveOrOtRequest, Integer> {
}