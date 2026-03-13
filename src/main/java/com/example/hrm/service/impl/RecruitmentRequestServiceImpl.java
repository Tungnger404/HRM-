package com.example.hrm.service.impl;

import com.example.hrm.dto.RecruitmentRequestCreateDTO;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.RecruitmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {

    private final RecruitmentRequestRepository recruitmentRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final EmployeeRepository employeeRepository;

    // ==========================================
    // 1️⃣ CREATE REQUEST (Updated with new fields)
    // ==========================================
    @Override
    public void createRecruitmentRequest(RecruitmentRequestCreateDTO dto) {

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        JobPosition jobPosition = jobPositionRepository.findById(dto.getJobId())
                .orElseThrow(() -> new RuntimeException("Job position not found"));

        Employee creator = employeeRepository.findById(dto.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Sử dụng Builder nếu bạn đã thêm @Builder vào Entity,
        // hoặc dùng Setter như cũ nhưng bổ sung các trường nghiệp vụ mới
        RecruitmentRequest request = new RecruitmentRequest();

        request.setDepartment(department);
        request.setJobPosition(jobPosition);
        request.setQuantity(dto.getQuantity());
        request.setReason(dto.getReason());

        // --- BỔ SUNG CÁC TRƯỜNG MỚI ĐÃ THỐNG NHẤT ---
        request.setTechnicalRequirements(dto.getTechnicalRequirements());
        request.setProposedSalary(dto.getProposedSalary());
        request.setPriority(dto.getPriority()); // HIGH, MEDIUM, LOW
        // ------------------------------------------

        if (dto.getDeadline() != null) {
            request.setDeadline(dto.getDeadline().atStartOfDay());
        }

        request.setStatus(RecruitmentRequestStatus.SUBMITTED);
        request.setCreatedBy(creator);
        request.setCreatedAt(LocalDateTime.now());

        recruitmentRequestRepository.save(request);
    }

    // ==========================================
    // 2️⃣ HR LIST & SEARCH
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentRequest> getRequestsForHR() {
        return recruitmentRequestRepository.findForHR();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentRequest> searchRequests(String keyword, RecruitmentRequestStatus status, String priority) {
        String searchKey = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String priorityKey = (priority != null && !priority.trim().isEmpty()) ? priority.trim() : null;
        return recruitmentRequestRepository.searchRequests(searchKey, status, priorityKey);
    }
    // ==========================================
    // 3️⃣ DETAIL & APPROVAL
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public RecruitmentRequest getById(Integer id) {
        return recruitmentRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + id));
    }

    @Override
    public void approveRequest(Integer id) {
        RecruitmentRequest request = getById(id);
        // Kiểm tra nếu request đã xử lý rồi thì không cho approve lại
        if (request.getStatus() != RecruitmentRequestStatus.SUBMITTED) {
            throw new RuntimeException("Only SUBMITTED requests can be approved.");
        }
        request.setStatus(RecruitmentRequestStatus.APPROVED);
        recruitmentRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(Integer id, String reason) {
        RecruitmentRequest request = getById(id);
        request.setStatus(RecruitmentRequestStatus.REJECTED);
        // Lưu lý do từ chối vào trường reason hoặc một trường note riêng
        request.setReason("REJECTED REASON: " + reason + " | Original Reason: " + request.getReason());
        recruitmentRequestRepository.save(request);
    }

    // ==========================================
    // 4️⃣ EMPLOYEE OWNED REQUESTS
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentRequest> getRequestsByEmployee(Integer empId) {
        return recruitmentRequestRepository.findByCreatedBy_EmpId(empId);
    }
}