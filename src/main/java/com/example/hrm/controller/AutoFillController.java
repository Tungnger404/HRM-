package com.example.hrm.controller;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

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
    public void create(JobDescriptionCreateDTO dto, Principal principal) {
        // 1. Tìm Request gốc
        RecruitmentRequest request = requestRepo.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 2. Map dữ liệu từ DTO và một phần từ Request sang JD
        JobDescription jd = JobDescription.builder()
                .recruitmentRequest(request)
                .job(request.getJobPosition()) // Lấy luôn Job từ Request
                .responsibilities(dto.getResponsibilities())
                .requirements(dto.getRequirements()) // HR đã chỉnh sửa từ technicalRequirements
                .benefits(dto.getBenefits())
                .salaryRange(dto.getSalaryRange())
                .workingLocation(dto.getWorkingLocation())
                .status(JobDescriptionStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        jdRepo.save(jd);

        // 3. Cập nhật trạng thái Request để không cho tạo JD lần thứ 2
        request.setStatus(RecruitmentRequestStatus.CLOSED);
        requestRepo.save(request);
    }
}