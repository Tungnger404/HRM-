package com.example.hrm.controller;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AutoFillController {

    private final RecruitmentRequestRepository requestRepo;
    private final JobDescriptionRepository jdRepo;

    // =============================
    // AUTO FILL FROM REQUEST
    // =============================
    @GetMapping("/request/{id}")
    public RecruitmentRequestResponseDTO getRequest(@PathVariable Integer id) {

        RecruitmentRequest request = requestRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        RecruitmentRequestResponseDTO dto = new RecruitmentRequestResponseDTO();

        dto.setReqId(request.getReqId());
        dto.setJobTitle(request.getJobPosition().getTitle());
        dto.setQuantity(request.getQuantity());
        dto.setDepartmentName(request.getDepartment().getDeptName());

        return dto;
    }

    // =============================
    // AUTO FILL FROM JD
    // =============================
    @GetMapping("/jd/{id}")
    public JobDescriptionResponseDTO getJD(@PathVariable Integer id) {

        JobDescription jd = jdRepo.findByIdWithJob(id)
                .orElseThrow(() -> new RuntimeException("JD not found"));

        JobDescriptionResponseDTO dto = new JobDescriptionResponseDTO();

        dto.setId(jd.getId());
        dto.setJobTitle(jd.getJob().getTitle());
        dto.setSalaryRange(jd.getSalaryRange());
        dto.setWorkingLocation(jd.getWorkingLocation());
        dto.setStatus(jd.getStatus());
        dto.setCreatedAt(jd.getCreatedAt());
        dto.setResponsibilities(jd.getResponsibilities());
        dto.setRequirements(jd.getRequirements());
        dto.setBenefits(jd.getBenefits());

        return dto;
    }
}