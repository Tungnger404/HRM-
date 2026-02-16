package com.example.hrm.service;

import com.example.hrm.dto.JobPostingCreateDTO;
import com.example.hrm.entity.JobPosting;

import java.util.List;

public interface JobPostingService {

    List<JobPosting> getAll();

    void create(JobPostingCreateDTO dto);

    void changeStatus(Integer id, String status);

    void delete(Integer id);

    void autoExpire();

}
