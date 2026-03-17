package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestStatus;
import com.example.hrm.entity.RequestType;
import com.example.hrm.repository.LeaveOrOtRequestRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LeaveOrOtRequestService {

    private final LeaveOrOtRequestRepository repository;

    public LeaveOrOtRequestService(LeaveOrOtRequestRepository repository) {
        this.repository = repository;
    }

    public LeaveOrOtRequest create(LeaveOrOtRequest request) {
        if (request.getRequestType() == null) {
            throw new RuntimeException("Request type is required.");
        }

        if (request.getTargetWorkDate() == null) {
            throw new RuntimeException("Target work date is required.");
        }

        if (request.getRequestType() == RequestType.LEAVE ||
                request.getRequestType() == RequestType.OVERTIME) {

            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new RuntimeException("Start time and End time are required.");
            }

            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new RuntimeException("Start time must be before End time.");
            }

            if (request.getRequestType() == RequestType.OVERTIME) {
                long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
                if (hours > 12) {
                    throw new RuntimeException("Overtime cannot exceed 12 hours.");
                }
            }
        }

        if (request.getRequestType() == RequestType.OTHER) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new RuntimeException("Reason is required for OTHER request.");
            }
            request.setStartTime(null);
            request.setEndTime(null);
        }

        if (request.getStatus() == null) {
            request.setStatus(RequestStatus.PENDING);
        }

        return repository.save(request);
    }
    public java.util.List<LeaveOrOtRequest> getMyRequests(Integer empId) {
        return repository.findByEmpIdOrderByCreatedAtDesc(empId);
    }
}
