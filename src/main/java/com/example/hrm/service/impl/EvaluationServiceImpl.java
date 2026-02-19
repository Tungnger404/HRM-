package com.example.hrm.service.impl;

import com.example.hrm.dto.EvaluationResultDTO;
import com.example.hrm.entity.*;
import com.example.hrm.entity.Evaluation.EvaluationStatus;
import com.example.hrm.entity.EvaluationEvidence.VerificationStatus;
import com.example.hrm.entity.EvaluationHistory.HistoryAction;
import com.example.hrm.repository.*;
import com.example.hrm.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    @Autowired
    private EvaluationEvidenceRepository evaluationEvidenceRepository;

    @Autowired
    private EvaluationHistoryRepository evaluationHistoryRepository;

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private KpiTemplateRepository kpiTemplateRepository;

    @Autowired
    private EvalCycleRepository evalCycleRepository;

    // === Employee ===

    @Override
    @Transactional
    public Evaluation createEvaluation(Integer empId, Integer cycleId) {
        // Check duplicate
        Optional<Evaluation> existing = evaluationRepository.findByEmpIdAndCycleId(empId, cycleId);
        if (existing.isPresent()) {
            throw new RuntimeException("Employee already has an evaluation for this cycle");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setEmpId(empId);
        evaluation.setCycleId(cycleId);
        evaluation.setStatus(EvaluationStatus.SELF_REVIEW);
        evaluation = evaluationRepository.save(evaluation);

        // Auto-create EvaluationDetail for each KPI assigned to this employee in this cycle
        List<KpiAssignment> assignments = kpiAssignmentRepository.findByEmpIdAndCycleId(empId, cycleId);
        for (KpiAssignment assignment : assignments) {
            EvaluationDetail detail = new EvaluationDetail();
            detail.setEvalId(evaluation.getEvalId());
            detail.setKpiId(assignment.getKpiId());
            detail.setSelfScore(BigDecimal.ZERO);
            detail.setManagerScore(BigDecimal.ZERO);
            detail.setFinalScore(BigDecimal.ZERO);
            evaluationDetailRepository.save(detail);
        }

        return evaluation;
    }

    @Override
    @Transactional
    public void submitSelfScore(Integer evalId, Integer kpiId, BigDecimal selfScore) {
        EvaluationDetail detail = evaluationDetailRepository.findByEvalIdAndKpiId(evalId, kpiId)
                .orElseThrow(() -> new RuntimeException("Evaluation detail not found"));

        detail.setSelfScore(selfScore);
        evaluationDetailRepository.save(detail);
    }

    @Override
    @Transactional
    public Evaluation submitEvaluation(Integer evalId, String comment) {
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        if (evaluation.getStatus() != EvaluationStatus.SELF_REVIEW) {
            throw new RuntimeException("Can only submit when status is SELF_REVIEW");
        }

        evaluation.setStatus(EvaluationStatus.MANAGER_REVIEW);
        evaluation = evaluationRepository.save(evaluation);

        // Log history
        logHistory(evalId, HistoryAction.SELF_SUBMIT, evaluation.getEmpId(), null, null, comment);

        return evaluation;
    }

    @Override
    public List<Evaluation> getEvaluationHistory(Integer empId) {
        return evaluationRepository.findByEmpIdOrderByEvalIdDesc(empId);
    }

    @Override
    public Evaluation getEvaluationById(Integer evalId) {
        return evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    // === Evidence ===

    @Override
    @Transactional
    public EvaluationEvidence uploadEvidence(Integer evalId, Integer kpiId, String fileUrl, String description) {
        // Verify evaluation exists
        evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        EvaluationEvidence evidence = new EvaluationEvidence();
        evidence.setEvalId(evalId);
        evidence.setKpiId(kpiId);
        evidence.setFileUrl(fileUrl);
        evidence.setDescription(description);
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setVerificationStatus(VerificationStatus.PENDING);

        return evaluationEvidenceRepository.save(evidence);
    }

    // === HR ===

    @Override
    @Transactional
    public EvaluationEvidence verifyEvidence(Integer evidenceId, Boolean isValid, Integer verifiedBy) {
        EvaluationEvidence evidence = evaluationEvidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));

        evidence.setVerifiedBy(verifiedBy);
        evidence.setVerifiedAt(LocalDateTime.now());
        evidence.setVerificationStatus(isValid ? VerificationStatus.APPROVED : VerificationStatus.REJECTED);

        return evaluationEvidenceRepository.save(evidence);
    }

    // === Manager ===

    @Override
    public List<Evaluation> getEvaluationsByManagerAndStatus(Integer managerId, EvaluationStatus status) {
        return evaluationRepository.findByManagerIdAndStatus(managerId, status);
    }

    @Override
    @Transactional
    public void submitManagerScore(Integer evalId, Integer kpiId, BigDecimal managerScore) {
        EvaluationDetail detail = evaluationDetailRepository.findByEvalIdAndKpiId(evalId, kpiId)
                .orElseThrow(() -> new RuntimeException("Evaluation detail not found"));

        BigDecimal oldScore = detail.getFinalScore();
        detail.setManagerScore(managerScore);
        detail.setFinalScore(managerScore); // final = manager score
        evaluationDetailRepository.save(detail);

        // Log history
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        logHistory(evalId, HistoryAction.MANAGER_EDIT, evaluation.getManagerId(), oldScore, managerScore, null);
    }

    @Override
    @Transactional
    public BigDecimal calculateTotalScore(Integer evalId) {
        List<EvaluationDetail> details = evaluationDetailRepository.findByEvalId(evalId);

        BigDecimal totalScore = BigDecimal.ZERO;

        for (EvaluationDetail detail : details) {
            // Get weight from kpi_templates
            Optional<KpiTemplate> kpiOpt = kpiTemplateRepository.findById(detail.getKpiId());
            if (kpiOpt.isPresent() && detail.getFinalScore() != null && kpiOpt.get().getWeight() != null) {
                // formula: finalScore * weight / 100
                BigDecimal weightedScore = detail.getFinalScore()
                        .multiply(kpiOpt.get().getWeight())
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                totalScore = totalScore.add(weightedScore);
            }
        }

        // Update evaluation
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        evaluation.setFinalScore(totalScore);
        evaluationRepository.save(evaluation);

        return totalScore;
    }

    @Override
    public String calculateClassification(BigDecimal totalScore) {
        if (totalScore == null) return "D";

        double score = totalScore.doubleValue();
        if (score >= 90) return "A";
        if (score >= 75) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    @Override
    @Transactional
    public Evaluation approveEvaluation(Integer evalId, String managerComment) {
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        if (evaluation.getStatus() != EvaluationStatus.MANAGER_REVIEW) {
            throw new RuntimeException("Can only approve when status is MANAGER_REVIEW");
        }

        // Calculate score & classification
        BigDecimal totalScore = calculateTotalScore(evalId);
        String classification = calculateClassification(totalScore);

        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setManagerScore(totalScore);
        evaluation.setClassification(classification);
        evaluation.setManagerComment(managerComment);
        evaluation = evaluationRepository.save(evaluation);

        // Log history
        logHistory(evalId, HistoryAction.MANAGER_APPROVE, evaluation.getManagerId(), null, totalScore, managerComment);

        return evaluation;
    }

    @Override
    @Transactional
    public Evaluation rejectEvaluation(Integer evalId, String managerComment) {
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        if (evaluation.getStatus() != EvaluationStatus.MANAGER_REVIEW) {
            throw new RuntimeException("Can only reject when status is MANAGER_REVIEW");
        }

        // Return to SELF_REVIEW for revision
        evaluation.setStatus(EvaluationStatus.SELF_REVIEW);
        evaluation.setManagerComment(managerComment);
        evaluation = evaluationRepository.save(evaluation);

        // Log history
        logHistory(evalId, HistoryAction.MANAGER_EDIT, evaluation.getManagerId(), null, null, managerComment);

        return evaluation;
    }

    // === Report ===

    @Override
    public List<Evaluation> getEmployeeEvaluations(Integer empId) {
        return evaluationRepository.findByEmpId(empId);
    }

    @Override
    public List<Evaluation> getEvaluationsByCycle(Integer cycleId) {
        return evaluationRepository.findByCycleId(cycleId);
    }

    // === Integration with Payroll Module ===

    @Override
    @Transactional(readOnly = true)
    public EvaluationResultDTO getEvaluationResultForPayroll(Integer employeeId, Integer cycleId) {
        // Find completed evaluation for employee in specific cycle
        Optional<Evaluation> evalOpt = evaluationRepository.findByEmpIdAndCycleId(employeeId, cycleId);
        
        if (evalOpt.isEmpty()) {
            return null; // No evaluation found
        }
        
        Evaluation evaluation = evalOpt.get();
        
        // Only return if completed
        if (evaluation.getStatus() != EvaluationStatus.COMPLETED) {
            return null; // Evaluation not completed yet
        }
        
        return buildEvaluationResultDTO(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResultDTO getEvaluationResultByPeriod(Integer employeeId, Integer year, Integer month) {
        // Find evaluation cycle by period
        // Assuming EvalCycle has year/month fields or you can query by date range
        List<Evaluation> evaluations = evaluationRepository.findByEmpId(employeeId);
        
        Evaluation matchingEval = evaluations.stream()
                .filter(e -> e.getStatus() == EvaluationStatus.COMPLETED)
                .filter(e -> matchesPeriod(e, year, month))
                .findFirst()
                .orElse(null);
        
        if (matchingEval == null) {
            return null; // No completed evaluation found for this period
        }
        
        return buildEvaluationResultDTO(matchingEval);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResultDTO getLatestEvaluationResult(Integer employeeId) {
        // Find latest completed evaluation using repository query
        List<Evaluation> evaluations = evaluationRepository.findByEmpIdOrderByEvalIdDesc(employeeId);
        
        Evaluation latestEval = evaluations.stream()
                .filter(e -> e.getStatus() == EvaluationStatus.COMPLETED)
                .findFirst()
                .orElse(null);
        
        if (latestEval == null) {
            return null; // No completed evaluation found
        }
        
        return buildEvaluationResultDTO(latestEval);
    }

    // === Private helpers ===

    private EvaluationResultDTO buildEvaluationResultDTO(Evaluation evaluation) {
        BigDecimal totalScore = evaluation.getFinalScore() != null 
                ? evaluation.getFinalScore() 
                : calculateTotalScore(evaluation.getEvalId());
        
        String classification = evaluation.getClassification() != null
                ? evaluation.getClassification()
                : calculateClassification(totalScore);
        
        String classificationLabel = getClassificationLabel(classification);
        BigDecimal bonusPercentage = getSuggestedBonusPercentage(classification);
        
        return EvaluationResultDTO.builder()
                .employeeId(evaluation.getEmpId())
                .employeeName(null) // Will be populated by caller if needed
                .evaluationId(evaluation.getEvalId())
                .cycleId(evaluation.getCycleId())
                .cycleName(null) // Will be populated by caller if needed
                .selfScore(evaluation.getSelfScore())
                .managerScore(evaluation.getManagerScore())
                .totalScore(totalScore)
                .classification(classification)
                .classificationLabel(classificationLabel)
                .status(evaluation.getStatus().name())
                .completedAt(null) // No timestamp field available in Evaluation entity
                .suggestedBonusPercentage(bonusPercentage)
                .build();
    }

    private String getClassificationLabel(String classification) {
        return switch (classification) {
            case "A" -> "Excellent";
            case "B" -> "Good";
            case "C" -> "Average";
            case "D" -> "Poor";
            default -> "Unknown";
        };
    }

    private BigDecimal getSuggestedBonusPercentage(String classification) {
        return switch (classification) {
            case "A" -> new BigDecimal("0.20");  // 20% bonus
            case "B" -> new BigDecimal("0.10");  // 10% bonus
            case "C" -> BigDecimal.ZERO;          // 0% bonus
            case "D" -> new BigDecimal("-0.05");  // -5% penalty
            default -> BigDecimal.ZERO;
        };
    }

    private boolean matchesPeriod(Evaluation evaluation, Integer year, Integer month) {
        if (evaluation.getCycleId() == null) {
            return false;
        }
        
        Optional<EvalCycle> cycleOpt = evalCycleRepository.findById(evaluation.getCycleId());
        if (cycleOpt.isEmpty()) {
            return false;
        }
        
        EvalCycle cycle = cycleOpt.get();
        LocalDate startDate = cycle.getStartDate();
        LocalDate endDate = cycle.getEndDate();
        
        if (startDate == null || endDate == null) {
            return false;
        }
        
        // Check if the cycle's period overlaps with the given year/month
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        
        // Cycle overlaps with the month if:
        // cycle.startDate <= monthEnd AND cycle.endDate >= monthStart
        return !startDate.isAfter(monthEnd) && !endDate.isBefore(monthStart);
    }

    private void logHistory(Integer evalId, HistoryAction action, Integer actionBy,
                            BigDecimal oldScore, BigDecimal newScore, String comment) {
        EvaluationHistory history = new EvaluationHistory();
        history.setEvalId(evalId);
        history.setAction(action);
        history.setActionBy(actionBy);
        history.setActionAt(LocalDateTime.now());
        history.setOldScore(oldScore);
        history.setNewScore(newScore);
        history.setComment(comment);
        evaluationHistoryRepository.save(history);
    }
}