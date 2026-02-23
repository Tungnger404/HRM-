package com.example.hrm.repository;

import com.example.hrm.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository
        extends JpaRepository<JobPosting, Integer> {

    List<JobPosting> findByStatus(String status);

}
