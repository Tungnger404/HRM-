package com.example.hrm.controller;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AutoFillController {

    private final RecruitmentRequestRepository requestRepo;
    private final JobDescriptionRepository jdRepo;


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


    @GetMapping("/jd/{id}")
    public ResponseEntity<?> getJd(@PathVariable Integer id) {
        return jdRepo.findById(id)
                .map(jd -> {
                    // Tạo một Map hoặc DTO đơn giản để trả về JSON phẳng
                    Map<String, Object> response = new HashMap<>();
                    response.put("title", jd.getJob().getTitle());
                    response.put("responsibilities", jd.getResponsibilities());
                    response.put("requirements", jd.getRequirements());
                    response.put("benefits", jd.getBenefits());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}