package com.example.hrm.service;

import com.example.hrm.dto.*;

import java.util.List;

public interface JobDescriptionService {

    void create(JobDescriptionCreateDTO dto);

    List<JobDescriptionResponseDTO> getAll();

    JobDescriptionResponseDTO getById(Integer id);

    void update(Integer id, JobDescriptionUpdateDTO dto);

    void changeStatus(Integer id, String status);

    void delete(Integer id);
}
