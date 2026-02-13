package com.example.hrm.service;

import com.example.hrm.dto.ChangeRequestForm;
import com.example.hrm.entity.EmployeeChangeRequest;

import java.util.List;

public interface EmployeeChangeRequestService {
    EmployeeChangeRequest submit(ChangeRequestForm form);
    List<EmployeeChangeRequest> myRequests(Integer employeeId);

    List<EmployeeChangeRequest> pending();
    void approve(Integer requestId, Integer approverUserId, String decisionNote);
    void reject(Integer requestId, Integer approverUserId, String decisionNote);
}
