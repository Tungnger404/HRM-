package com.example.hrm.repository;

import com.example.hrm.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPositionRepository
        extends JpaRepository<JobPosition, Integer> {
}
