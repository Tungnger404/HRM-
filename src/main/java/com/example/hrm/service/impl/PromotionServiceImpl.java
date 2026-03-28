package com.example.hrm.service.impl;

import com.example.hrm.dto.PromotionReviewDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.PromotionRequest;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.PromotionRequestRepository;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final int ELIGIBLE_MIN_EVALUATION_COUNT = 2;
    private static final int ELIGIBLE_MIN_A_CLASSIFICATION_COUNT = 2;
    private static final DateTimeFormatter PROMOTION_EFFECTIVE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final PromotionRequestRepository promotionRequestRepository;
    private final NotificationService notificationService;

    @Override
    public List<Map<String, Object>> getEligibleEmployeesForPromotion(Integer requesterId) {
        List<Employee> allEmployees = employeeRepository.findByDirectManagerId(requesterId);

        return allEmployees.stream()
                .filter(emp -> !emp.getEmpId().equals(requesterId))
                .filter(emp -> !isInactiveForPromotion(emp.getStatus()))
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
                    result.put("currentPositionTitle", resolvePositionTitle(emp.getJobId()));
                    result.put("currentPositionLevel", resolvePositionLevel(emp.getJobId()));
                    result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
                    result.put("evaluationCount", completedEvals.size());
                    boolean eligible = isEligible(completedEvals);
                    result.put("isEligible", eligible);
                    result.put("validPromotionOptions", getValidPromotionPositions(emp.getJobId()).size());

                    return result;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("avgScore"), (Double) a.get("avgScore")))
                .collect(Collectors.toList());
    }

    @Override
    public PromotionReviewDTO getEmployeeEvaluationHistory(Integer empId) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<KpiAssignment> completedEvals = kpiAssignmentRepository
                .findByEmpIdAndStatusOrderByManagerReviewedAtDesc(empId, KpiAssignment.AssignmentStatus.COMPLETED)
                .stream()
                .filter(a -> a.getManagerScore() != null)
                .limit(12)
                .toList();

        double avgScore = completedEvals.isEmpty() ? 0.0 :
                completedEvals.stream()
                        .mapToDouble(KpiAssignment::getManagerScore)
                        .average()
                        .orElse(0.0);

        List<PromotionReviewDTO.EvaluationRow> evalRows = completedEvals.stream()
                .map(eval -> PromotionReviewDTO.EvaluationRow.builder()
                        .cycleId(eval.getCycleId())
                        .managerScore(eval.getManagerScore())
                        .finalScore(eval.getManagerScore())
                        .classification(eval.getClassification())
                        .status(eval.getStatus() != null ? eval.getStatus().name() : "N/A")
                        .reviewedAt(eval.getManagerReviewedAt())
                        .build())
                .toList();

        return PromotionReviewDTO.builder()
                .employee(PromotionReviewDTO.EmployeeInfo.builder()
                        .empId(employee.getEmpId())
                        .fullName(employee.getFullName())
                        .department(resolveDepartmentName(employee.getDeptId()))
                        .jobTitle(resolvePositionTitle(employee.getJobId()))
                        .directManager(resolveManagerName(employee.getDirectManagerId()))
                        .build())
                .evaluations(evalRows)
                .avgScore(Math.round(avgScore * 10.0) / 10.0)
                .evaluationCount(completedEvals.size())
                .hasEvaluations(!completedEvals.isEmpty())
                .emptyMessage(completedEvals.isEmpty()
                        ? "No evaluation information available for this employee."
                        : null)
                .build();
    }

    @Override
    @Transactional
    public PromotionRequest submitPromotionRequest(Integer empId, Integer proposedPositionId,
                                                   String reason, Integer requestedBy) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (proposedPositionId == null) {
            throw new RuntimeException("Proposed position is required");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required");
        }
        if (Objects.equals(employee.getJobId(), proposedPositionId)) {
            throw new RuntimeException("Proposed position must be different from current position");
        }
        if (promotionRequestRepository.existsByEmpIdAndStatus(empId, PromotionRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Employee already has a pending promotion request");
        }

        JobPosition currentPosition = employee.getJobId() != null
                ? jobPositionRepository.findById(employee.getJobId()).orElse(null)
                : null;
        JobPosition proposedPosition = jobPositionRepository.findById(proposedPositionId)
                .orElseThrow(() -> new RuntimeException("Proposed position not found"));

        if (!Boolean.TRUE.equals(proposedPosition.getActive())) {
            throw new RuntimeException("Proposed position is inactive");
        }

        if (!isValidHigherPosition(currentPosition, proposedPosition)) {
            throw new RuntimeException("Proposed position level must be higher than current position level");
        }

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

        if (!isEligible(completedEvals)) {
            throw new RuntimeException("Employee is not eligible for promotion yet (minimum 2 completed manager evaluations with classification A)");
        }

        String evalSummary = String.format(
                "Average Final Score (manager): %.1f/100 | Total Evaluations: %d | Recent Classifications: %s",
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

        for (Employee hrStaff : employeeRepository.findHrStaff()) {
            notificationService.create(
                    hrStaff.getEmpId(),
                    "PROMOTION_REQUEST",
                    "New Promotion Request",
                    employee.getFullName() + " has been recommended for promotion",
                    "/hr/promotions/pending"
            );
        }

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

        String promotedPositionTitle = resolvePositionTitle(request.getProposedPositionId());
        String effectiveDate = request.getReviewedAt() != null
                ? request.getReviewedAt().format(PROMOTION_EFFECTIVE_DATE_FORMAT)
                : LocalDateTime.now().format(PROMOTION_EFFECTIVE_DATE_FORMAT);

        notificationService.create(
                request.getEmpId(),
                "PROMOTION_APPROVED_EMPLOYEE",
                "Promotion Approved",
                "Congratulations! You have been promoted to " + promotedPositionTitle
                        + " effective from " + effectiveDate
                        + ". Please check your profile to see your Current Position",
                "/employee/profile"
        );

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

    private boolean isEligible(List<KpiAssignment> completedEvals) {
        if (completedEvals == null || completedEvals.size() < ELIGIBLE_MIN_EVALUATION_COUNT) {
            return false;
        }
        long aCount = completedEvals.stream()
                .map(KpiAssignment::getClassification)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter("A"::equals)
                .count();
        return aCount >= ELIGIBLE_MIN_A_CLASSIFICATION_COUNT;
    }

    @Override
    public List<JobPosition> getValidPromotionPositions(Integer currentJobId) {
        JobPosition currentPosition = currentJobId != null
                ? jobPositionRepository.findById(currentJobId).orElse(null)
                : null;

        return jobPositionRepository.findByActiveTrueOrderByTitleAsc().stream()
                .filter(pos -> currentJobId == null || !Objects.equals(pos.getJobId(), currentJobId))
                .filter(pos -> isValidHigherPosition(currentPosition, pos))
                .toList();
    }

    private boolean isValidHigherPosition(JobPosition currentPosition, JobPosition proposedPosition) {
        if (proposedPosition == null || !Boolean.TRUE.equals(proposedPosition.getActive())) {
            return false;
        }
        if (currentPosition == null) {
            return true;
        }
        Integer currentLevel = currentPosition.getJobLevel();
        Integer proposedLevel = proposedPosition.getJobLevel();
        if (currentLevel == null || proposedLevel == null) {
            return false;
        }
        return proposedLevel > currentLevel;
    }

    private String resolveDepartmentName(Integer deptId) {
        if (deptId == null) {
            return "N/A";
        }
        return departmentRepository.findById(deptId)
                .map(d -> d.getDeptName())
                .orElse("N/A");
    }

    private String resolvePositionTitle(Integer jobId) {
        if (jobId == null) {
            return "N/A";
        }
        return jobPositionRepository.findById(jobId)
                .map(JobPosition::getTitle)
                .orElse("N/A");
    }

    private Integer resolvePositionLevel(Integer jobId) {
        if (jobId == null) {
            return null;
        }
        return jobPositionRepository.findById(jobId)
                .map(JobPosition::getJobLevel)
                .orElse(null);
    }

    private boolean isInactiveForPromotion(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return normalized.contains("TERMIN")
                || normalized.contains("RESIGN")
                || normalized.equals("INACTIVE")
                || normalized.equals("QUIT");
    }

    private String resolveManagerName(Integer managerId) {
        if (managerId == null) {
            return "N/A";
        }
        return employeeRepository.findById(managerId)
                .map(Employee::getFullName)
                .orElse("N/A");
    }
}


