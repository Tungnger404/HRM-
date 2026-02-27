package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestType;
import com.example.hrm.entity.RequestStatus;
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

        // =====================================
        // Validate Request Type
        // =====================================
        if (request.getRequestType() == null) {
            throw new RuntimeException("Request type is required.");
        }

        // =====================================
        // Validate LEAVE & OVERTIME
        // =====================================
        if (request.getRequestType() == RequestType.LEAVE ||
                request.getRequestType() == RequestType.OVERTIME) {

            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new RuntimeException("Start time and End time are required.");
            }

            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new RuntimeException("Start time must be before End time.");
            }

            // Nếu là OVERTIME → kiểm tra tối đa 12 giờ
            if (request.getRequestType() == RequestType.OVERTIME) {

                long hours = Duration.between(
                        request.getStartTime(),
                        request.getEndTime()
                ).toHours();

                if (hours > 12) {
                    throw new RuntimeException("Overtime cannot exceed 12 hours.");
                }
            }
        }

        // =====================================
        // Validate OTHER
        // =====================================
        if (request.getRequestType() == RequestType.OTHER) {

            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new RuntimeException("Reason is required for OTHER request.");
            }

            // đảm bảo không lưu time
            request.setStartTime(null);
            request.setEndTime(null);
        }

        // =====================================
        // Default Status (nếu chưa set)
        // =====================================
        if (request.getStatus() == null) {
            request.setStatus(RequestStatus.PENDING);
        }

        return repository.save(request);
    }
}