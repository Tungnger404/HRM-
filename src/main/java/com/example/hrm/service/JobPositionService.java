package com.example.hrm.service;

import com.example.hrm.dto.JobPositionForm;
import com.example.hrm.entity.JobPosition;
import org.springframework.data.domain.*;

public interface JobPositionService {

    Page<JobPosition> search(String q, Integer level, Boolean active, int page, int size);

    JobPosition getById(Integer id);

    JobPositionForm getFormById(Integer id);

    Integer create(JobPositionForm form);

    void update(Integer id, JobPositionForm form);

    void setActive(Integer id, boolean active);
}