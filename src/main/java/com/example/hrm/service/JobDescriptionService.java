package com.example.hrm.service;

import com.example.hrm.dto.*;
import com.example.hrm.entity.JobDescriptionStatus;

import java.security.Principal;
import java.util.List;

public interface JobDescriptionService {

    void create(JobDescriptionCreateDTO dto, Principal principal);

    List<JobDescriptionResponseDTO> getAll();

    JobDescriptionResponseDTO getById(Integer id);

    void update(Integer id, JobDescriptionUpdateDTO dto);

    void changeStatus(Integer id, JobDescriptionStatus status);

    void delete(Integer id);
}
