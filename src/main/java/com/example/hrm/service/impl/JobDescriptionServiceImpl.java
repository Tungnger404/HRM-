package com.example.hrm.service.impl;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {

    private final JobDescriptionRepository repository;
    private final JobPositionRepository jobRepository;
    private final RecruitmentRequestRepository requestRepo;
    private final CurrentEmployeeService currentEmployeeService;

    @Override
    public JobDescriptionCreateDTO prepareCreateDTO(Integer requestId) {
        RecruitmentRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Recruitment Request không tồn tại!"));

        JobDescriptionCreateDTO dto = new JobDescriptionCreateDTO();

        dto.setRequestId(requestId);
        dto.setJobId(request.getJobPosition().getJobId());
        dto.setJobTitle(request.getJobPosition().getTitle());
        dto.setDescription("Linked to Request #" + requestId);
        dto.setSalaryRange(request.getProposedSalary());
        dto.setRequirements(request.getTechnicalRequirements());
        dto.setWorkingLocation("Văn phòng chính");
        dto.setResponsibilities("");
        dto.setBenefits("");

        return dto;
    }

    @Override
    @Transactional
    public void create(JobDescriptionCreateDTO dto, Principal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");

        RecruitmentRequest request = requestRepo.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getJobDescription() != null) {
            throw new RuntimeException("Yêu cầu này đã có bản mô tả công việc (JD).");
        }

        // --- TÍNH NĂNG VALIDATION DOANH NGHIỆP ---
        // Ví dụ: Kiểm tra lương nhập vào JD không vượt quá ngân sách của Request (nếu cần)

        Employee creator = currentEmployeeService.requireEmployee(principal);

        // --- TÍNH NĂNG AUDIT & ABC MAPPING ---
        JobDescription jd = new JobDescription();
        jd.setRecruitmentRequest(request);
        jd.setJob(request.getJobPosition()); // Đảm bảo khớp 100% với vị trí Manager yêu cầu

        // Nội dung chi tiết (Phần B)
        jd.setResponsibilities(dto.getResponsibilities());
        jd.setRequirements(dto.getRequirements());
        jd.setBenefits(dto.getBenefits());

        // Thông tin metadata (Phần A & C)
        jd.setSalaryRange(dto.getSalaryRange());
        jd.setWorkingLocation(dto.getWorkingLocation());

        // Audit Trail
        jd.setCreatedBy(creator);
        jd.setCreatedAt(LocalDateTime.now());
        jd.setStatus(JobDescriptionStatus.ACTIVE);

        repository.save(jd);

        // Đổi trạng thái request gốc để đánh dấu đã hoàn tất quy trình chuẩn bị
        // request.setStatus(RecruitmentRequestStatus.JD_CREATED);
        requestRepo.save(request);
    }

    @Override
    public List<JobDescriptionResponseDTO> getAll() {
        return repository.findAll().stream()
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
    @Transactional
    public void update(Integer id, JobDescriptionUpdateDTO dto) {
        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JD not found"));

        // Cập nhật nội dung ABC
        jd.setResponsibilities(dto.getResponsibilities());
        jd.setRequirements(dto.getRequirements());
        jd.setBenefits(dto.getBenefits());
        jd.setSalaryRange(dto.getSalaryRange());
        jd.setWorkingLocation(dto.getWorkingLocation());
        jd.setStatus(dto.getStatus());

        repository.save(jd);
    }

    @Override
    @Transactional
    public void changeStatus(Integer id, JobDescriptionStatus status) {
        JobDescription jd = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JD không tồn tại"));
        jd.setStatus(status);
        repository.save(jd);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    /**
     * MAPPING TO DTO: Hiển thị thông tin Audit và liên kết
     */
    private JobDescriptionResponseDTO mapToDTO(JobDescription jd) {
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

        if (jd.getRecruitmentRequest() != null) {
            dto.setRequestId(jd.getRecruitmentRequest().getReqId());

            String approver = (jd.getRecruitmentRequest().getApprovedBy() != null)
                    ? jd.getRecruitmentRequest().getApprovedBy().getFullName()
                    : "N/A";
            dto.setDescription("Phê duyệt bởi: " + approver);
        }

        return dto;
    }
}