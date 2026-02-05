package com.example.hrm.service;

import com.example.hrm.dto.RecruitmentRequestCreateDTO;
import com.example.hrm.entity.RecruitmentRequest;
import java.util.List;

public interface RecruitmentRequestService {
    //Màn 1
    void createRecruitmentRequest(RecruitmentRequestCreateDTO dto);
    // màn 2
    List<RecruitmentRequest> getRequestsForHR();
}
