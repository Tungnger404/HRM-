package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.repository.LeaveOrOtRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class LeaveOrOtRequestService {

    private final LeaveOrOtRequestRepository repository;

    public LeaveOrOtRequestService(LeaveOrOtRequestRepository repository) {
        this.repository = repository;
    }

    public LeaveOrOtRequest create(LeaveOrOtRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before End time");
        }

        return repository.save(request);
    }
}