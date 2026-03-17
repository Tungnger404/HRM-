package com.example.hrm.service;

import com.example.hrm.entity.*;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.LeaveOrOtRequestRepository;
import com.example.hrm.repository.ShiftAssignmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RequestWorkflowService {

    private final LeaveOrOtRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    public RequestWorkflowService(LeaveOrOtRequestRepository requestRepository,
                                  EmployeeRepository employeeRepository,
                                  ShiftAssignmentRepository shiftAssignmentRepository,
                                  AttendanceLogRepository attendanceLogRepository) {
        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.attendanceLogRepository = attendanceLogRepository;
    }

    public List<LeaveOrOtRequest> managerPendingRequests(Integer managerEmpId) {
        return requestRepository.findManagerRequestsByStatus(managerEmpId, RequestStatus.PENDING);
    }

    public void managerApprove(Integer managerEmpId, Integer requestId, String note) {
        LeaveOrOtRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        validateManagerCanHandle(managerEmpId, req);
        validatePending(req);

        req.setStatus(RequestStatus.APPROVED);
        req.setApproverId(managerEmpId);
        req.setApproverNote(note);
        req.setManagerDecidedAt(LocalDateTime.now());

        requestRepository.save(req);
    }

    public void managerReject(Integer managerEmpId, Integer requestId, String note) {
        LeaveOrOtRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        validateManagerCanHandle(managerEmpId, req);
        validatePending(req);

        req.setStatus(RequestStatus.REJECTED);
        req.setApproverId(managerEmpId);
        req.setApproverNote(note);
        req.setManagerDecidedAt(LocalDateTime.now());

        requestRepository.save(req);
    }

    public List<LeaveOrOtRequest> hrApprovedNotProcessed() {
        return requestRepository.findByStatusAndProcessedAtIsNullOrderByCreatedAtDesc(RequestStatus.APPROVED);
    }

    public List<LeaveOrOtRequest> hrManagerDecidedRequests() {
        return requestRepository.findByStatusInOrderByManagerDecidedAtDesc(
                List.of(RequestStatus.APPROVED, RequestStatus.REJECTED)
        );
    }

    public void hrProcess(Integer hrEmpId, Integer requestId) {
        LeaveOrOtRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RequestStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED requests can be processed by HR.");
        }
        if (req.getProcessedAt() != null) {
            throw new RuntimeException("Request already processed by HR.");
        }

        switch (req.getRequestType()) {
            case LEAVE -> processLeave(req);
            case OVERTIME, OTHER -> {
                // No attendance/schedule mutation needed for current phase
            }
            default -> throw new RuntimeException("Unsupported request type.");
        }

        req.setProcessedByHr(hrEmpId);
        req.setProcessedAt(LocalDateTime.now());
        requestRepository.save(req);
    }

    private void processLeave(LeaveOrOtRequest req) {
        LocalDate workDate = requireTargetDate(req);

        ShiftAssignment assignment = shiftAssignmentRepository
                .findByEmployee_EmpIdAndWorkDate(req.getEmpId(), workDate)
                .orElseThrow(() -> new RuntimeException("Shift assignment not found for leave date."));

        assignment.setAssignmentType("LEAVE");
        assignment.setShiftTemplate(null);
        shiftAssignmentRepository.save(assignment);

        req.setRelatedAssignmentId(assignment.getAssignmentId());

        attendanceLogRepository.findByEmployee_EmpIdAndWorkDate(req.getEmpId(), workDate).ifPresent(log -> {
            log.setStatus("LEAVE");
            log.setWorkType("LEAVE");
            attendanceLogRepository.save(log);
        });
    }

    private void validatePending(LeaveOrOtRequest req) {
        if (req.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Only PENDING request can be decided by manager.");
        }
    }

    private void validateManagerCanHandle(Integer managerEmpId, LeaveOrOtRequest req) {
        Employee requester = employeeRepository.findById(req.getEmpId())
                .orElseThrow(() -> new RuntimeException("Requester employee not found"));

        if (requester.getDirectManagerId() == null || !requester.getDirectManagerId().equals(managerEmpId)) {
            throw new RuntimeException("You are not allowed to approve/reject this request.");
        }
    }

    private LocalDate requireTargetDate(LeaveOrOtRequest req) {
        if (req.getTargetWorkDate() == null) {
            throw new RuntimeException("target_work_date is required.");
        }
        return req.getTargetWorkDate();
    }
}
