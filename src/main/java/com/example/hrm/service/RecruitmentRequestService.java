package com.example.hrm.service;

import com.example.hrm.dto.RecruitmentRequestCreateDTO;
import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.entity.RecruitmentRequestStatus;

import java.util.List;

public interface RecruitmentRequestService {

    // tạo request
    void createRecruitmentRequest(RecruitmentRequestCreateDTO dto);

    // HR list
    List<RecruitmentRequest> getRequestsForHR();

    // search + filter
    List<RecruitmentRequest> searchRequests(String keyword,
                                            RecruitmentRequestStatus status,
                                            String priority);

    // detail
    RecruitmentRequest getById(Integer id);

    // approve
    void approveRequest(Integer id);

    // reject
    void rejectRequest(Integer id, String reason);

    // request theo employee
    List<RecruitmentRequest> getRequestsByEmployee(Integer empId);

}