package com.example.hrm.service;

import com.example.hrm.dto.JobPostingCreateDTO;
import com.example.hrm.entity.JobPosting;

import java.util.List;

public interface JobPostingService {

    // ===== HR MANAGEMENT =====
    List<JobPosting> getAll();

    void create(JobPostingCreateDTO dto);

    void changeStatus(Integer id, String status);

    void delete(Integer id);

    void autoExpire();


    // ===== PUBLIC CAREER PAGE =====
    List<JobPosting> getPublicOpenJobs();

    JobPosting getBySlug(String slug);

    void increaseViewCount(String slug);

}