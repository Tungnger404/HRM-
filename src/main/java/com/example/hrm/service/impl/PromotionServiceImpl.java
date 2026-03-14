package com.example.hrm.service.impl;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.PromotionRequest;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.PromotionRequestRepository;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final EmployeeRepository employeeRepository;
    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final PromotionRequestRepository promotionRequestRepository;
    private final NotificationService notificationService;

    @Override
    public List<Map<String, Object>> getEligibleEmployeesForPromotion(Integer requesterId) {
        List<Employee> allEmployees = employeeRepository.findByDirectManagerId(requesterId);

        return allEmployees.stream()
                .filter(emp -> !emp.getEmpId().equals(requesterId))
                .filter(emp -> "OFFICIAL".equalsIgnoreCase(emp.getStatus())
                        || "ACTIVE".equalsIgnoreCase(emp.getStatus())
                        || "WORKING".equalsIgnoreCase(emp.getStatus()))
                .map(emp -> {
                    List<KpiAssignment> completedEvals = kpiAssignmentRepository.findByEmpId(emp.getEmpId())
                            .stream()
                            .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                            .filter(a -> a.getManagerScore() != null)
                            .toList();

                    double avgScore = completedEvals.isEmpty() ? 0.0 :
                            completedEvals.stream()
                                    .mapToDouble(KpiAssignment::getManagerScore)
                                    .average()
                                    .orElse(0.0);

                    Map<String, Object> result = new HashMap<>();
                    result.put("empId", emp.getEmpId());
                    result.put("fullName", emp.getFullName());
                    result.put("currentPosition", emp.getJobId());
                    result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
                    result.put("evaluationCount", completedEvals.size());
                    result.put("isEligible", avgScore >= 80.0 && completedEvals.size() >= 2);

                    return result;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("avgScore"), (Double) a.get("avgScore")))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getEmployeeEvaluationHistory(Integer empId) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<KpiAssignment> completedEvals = kpiAssignmentRepository.findByEmpId(empId)
                .stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .filter(a -> a.getManagerScore() != null)
                .sorted(Comparator.comparing(
                        KpiAssignment::getManagerReviewedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(12)
                .toList();

        double avgScore = completedEvals.isEmpty() ? 0.0 :
                completedEvals.stream()
                        .mapToDouble(KpiAssignment::getManagerScore)
                        .average()
                        .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("employee", employee);
        result.put("evaluations", completedEvals);
        result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        result.put("evaluationCount", completedEvals.size());

        return result;
    }

    @Override
    @Transactional
    public PromotionRequest submitPromotionRequest(Integer empId, Integer proposedPositionId,
                                                   String reason, Integer requestedBy) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<KpiAssignment> completedEvals = kpiAssignmentRepository.findByEmpId(empId)
                .stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .filter(a -> a.getManagerScore() != null)
                .toList();

        double avgScore = completedEvals.isEmpty() ? 0.0 :
                completedEvals.stream()
                        .mapToDouble(KpiAssignment::getManagerScore)
                        .average()
                        .orElse(0.0);

        String evalSummary = String.format(
                "Average Score: %.1f/100 | Total Evaluations: %d | Recent Classifications: %s",
                avgScore,
                completedEvals.size(),
                completedEvals.stream()
                        .limit(3)
                        .map(KpiAssignment::getClassification)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "))
        );

        PromotionRequest request = PromotionRequest.builder()
                .empId(empId)
                .currentPositionId(employee.getJobId())
                .proposedPositionId(proposedPositionId)
                .requestedBy(requestedBy)
                .requestReason(reason)
                .evaluationSummary(evalSummary)
                .avgScore(avgScore)
                .evaluationCount(completedEvals.size())
                .status(PromotionRequest.RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        PromotionRequest saved = promotionRequestRepository.save(request);

        // Tạm hardcode HR emp_id để test, sau này đổi sang tìm theo role HR
        notificationService.create(
                104,
                "PROMOTION_REQUEST",
                "New Promotion Request",
                employee.getFullName() + " has been recommended for promotion",
                "/hr/promotions/pending"
        );

        return saved;
    }

    @Override
    public List<PromotionRequest> getPendingPromotionRequests() {
        return promotionRequestRepository.findByStatusOrderByRequestedAtDesc(
                PromotionRequest.RequestStatus.PENDING
        );
    }

    @Override
    @Transactional
    public PromotionRequest approvePromotionRequest(Integer requestId, Integer reviewerId, String comment) {
        PromotionRequest request = promotionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Promotion request not found"));

        request.setStatus(PromotionRequest.RequestStatus.APPROVED);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request.setHrComment(comment);

        // Update job cho employee khi approve
        Employee employee = employeeRepository.findById(request.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setJobId(request.getProposedPositionId());
        employeeRepository.save(employee);

        PromotionRequest saved = promotionRequestRepository.save(request);

        notificationService.create(
                request.getRequestedBy(),
                "PROMOTION_APPROVED",
                "Promotion Request Approved",
                "Your promotion request for employee ID " + request.getEmpId() + " has been approved by HR",
                "/manager/promotion/my-requests"
        );

        return saved;
    }

    @Override
    @Transactional
    public PromotionRequest rejectPromotionRequest(Integer requestId, Integer reviewerId, String comment) {
        PromotionRequest request = promotionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Promotion request not found"));

        request.setStatus(PromotionRequest.RequestStatus.REJECTED);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request.setHrComment(comment);

        PromotionRequest saved = promotionRequestRepository.save(request);

        notificationService.create(
                request.getRequestedBy(),
                "PROMOTION_REJECTED",
                "Promotion Request Rejected",
                "Your promotion request for employee ID " + request.getEmpId() + " has been rejected",
                "/manager/promotion/my-requests"
        );

        return saved;
    }

    @Override
    public List<PromotionRequest> getMyPromotionRequests(Integer requestedBy) {
        return promotionRequestRepository.findByRequestedByOrderByRequestedAtDesc(requestedBy);
    }
}