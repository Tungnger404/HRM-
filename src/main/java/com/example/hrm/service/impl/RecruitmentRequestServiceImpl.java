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

    // CREATE REQUEST
    @Override
    public void createRecruitmentRequest(RecruitmentRequestCreateDTO dto) {

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        JobPosition jobPosition = jobPositionRepository.findById(dto.getJobId())
                .orElseThrow(() -> new RuntimeException("Job position not found"));

        Employee creator = employeeRepository.findById(dto.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        RecruitmentRequest request = new RecruitmentRequest();

        request.setDepartment(department);
        request.setJobPosition(jobPosition);
        request.setQuantity(dto.getQuantity());
        request.setReason(dto.getReason());
        request.setDeadline(dto.getDeadline().atStartOfDay());

        request.setStatus(RecruitmentRequestStatus.SUBMITTED);
        request.setCreatedBy(creator);
        request.setCreatedAt(LocalDateTime.now());

        recruitmentRequestRepository.save(request);
    }

    // HR LIST
    @Override
    public List<RecruitmentRequest> getRequestsForHR() {
        return recruitmentRequestRepository.findForHR();
    }

    // SEARCH + FILTER
    @Override
    public List<RecruitmentRequest> searchRequests(String keyword,
                                                   RecruitmentRequestStatus status) {

        if(keyword != null && keyword.trim().isEmpty()){
            keyword = null;
        }

        return recruitmentRequestRepository.searchRequests(keyword, status);
    }

    // DETAIL
    @Override
    public RecruitmentRequest getById(Integer id) {
        return recruitmentRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    // APPROVE
    @Override
    public void approveRequest(Integer id) {

        RecruitmentRequest request = getById(id);

        request.setStatus(RecruitmentRequestStatus.APPROVED);

        recruitmentRequestRepository.save(request);
    }

    // REJECT
    @Override
    public void rejectRequest(Integer id, String reason) {

        RecruitmentRequest request = getById(id);

        request.setStatus(RecruitmentRequestStatus.REJECTED);
        request.setReason(reason);

        recruitmentRequestRepository.save(request);
    }

    // REQUEST BY EMPLOYEE
    @Override
    public List<RecruitmentRequest> getRequestsByEmployee(Integer empId) {
        return recruitmentRequestRepository.findByCreatedBy_EmpId(empId);
    }
}