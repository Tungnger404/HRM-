package com.example.hrm.repository;

import com.example.hrm.entity.JobDescription;
import com.example.hrm.entity.JobDescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Integer> {

    List<JobDescription> findByStatus(JobDescriptionStatus status);

}

