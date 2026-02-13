package com.example.hrm.service.impl;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {

    private final JobDescriptionRepository repository;
    private final JobPositionRepository jobRepository;
    private final CurrentEmployeeService currentEmployeeService;

    @Override
    public void create(JobDescriptionCreateDTO dto) {

        JobPosition job = jobRepository.findById(dto.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Employee creator = currentEmployeeService.getCurrentEmployee();

        JobDescription jd = new JobDescription();
        jd.setJob(job);
        jd.setResponsibilities(dto.getResponsibilities());
        jd.setRequirements(dto.getRequirements());
        jd.setBenefits(dto.getBenefits());
        jd.setSalaryRange(dto.getSalaryRange());
        jd.setWorkingLocation(dto.getWorkingLocation());
        jd.setCreatedBy(creator);
        jd.setCreatedAt(LocalDateTime.now());
        jd.setStatus("ACTIVE");

        repository.save(jd);
    }

    @Override
    public List<JobDescriptionResponseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public JobDescriptionResponseDTO getById(Integer id) {
        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JD not found"));
        return mapToDTO(jd);
    }

    @Override
    public void update(Integer id, JobDescriptionUpdateDTO dto) {

        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JD not found"));

        jd.setResponsibilities(dto.getResponsibilities());
        jd.setRequirements(dto.getRequirements());
        jd.setBenefits(dto.getBenefits());
        jd.setSalaryRange(dto.getSalaryRange());
        jd.setWorkingLocation(dto.getWorkingLocation());
        jd.setStatus(dto.getStatus());

        repository.save(jd);
    }

    @Override
    public void changeStatus(Integer id, String status) {

        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JD not found"));

        jd.setStatus(status);
        repository.save(jd);
    }

    @Override
    public void delete(Integer id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException("JD not found");
        }

        repository.deleteById(id);
    }

    private JobDescriptionResponseDTO mapToDTO(JobDescription jd) {

        JobDescriptionResponseDTO dto = new JobDescriptionResponseDTO();
        dto.setId(jd.getId());
        dto.setJobTitle(jd.getJob().getTitle());
        dto.setSalaryRange(jd.getSalaryRange());
        dto.setWorkingLocation(jd.getWorkingLocation());
        dto.setStatus(jd.getStatus());
        dto.setCreatedAt(jd.getCreatedAt());

        return dto;
    }
}
