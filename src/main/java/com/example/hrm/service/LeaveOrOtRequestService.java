package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;

import java.util.List;

public interface LeaveOrOtRequestService {
    LeaveOrOtRequest create(LeaveOrOtRequest request);
    List<LeaveOrOtRequest> getMyRequests(Integer empId);
}