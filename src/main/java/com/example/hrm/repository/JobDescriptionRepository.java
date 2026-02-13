package com.example.hrm.repository;

import com.example.hrm.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Integer> {
}
