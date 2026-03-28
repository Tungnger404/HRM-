package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;

import java.util.List;

public interface RequestWorkflowService {
    List<LeaveOrOtRequest> managerPendingRequests(Integer managerEmpId);
    void managerApprove(Integer managerEmpId, Integer requestId, String note);
    void managerReject(Integer managerEmpId, Integer requestId, String note);
    List<LeaveOrOtRequest> hrPendingRequests();
    void hrApprove(Integer hrEmpId, Integer requestId, String note);
    void hrReject(Integer hrEmpId, Integer requestId, String note);
    List<LeaveOrOtRequest> hrApprovedNotProcessed();
    List<LeaveOrOtRequest> hrManagerDecidedRequests();
    void hrProcess(Integer hrEmpId, Integer requestId);
}