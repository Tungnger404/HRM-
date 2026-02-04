package com.example.hrm.evaluation.service;

import com.example.hrm.evaluation.model.Evaluation;
import com.example.hrm.evaluation.model.Evaluation.EvaluationStatus;
import com.example.hrm.evaluation.model.EvaluationEvidence;  // ✅ THÊM IMPORT

import java.math.BigDecimal;
import java.util.List;

public interface EvaluationService {

    // === Employee ===
    Evaluation createEvaluation(Integer empId, Integer cycleId);

    void submitSelfScore(Integer evalId, Integer kpiId, BigDecimal selfScore);

    Evaluation submitEvaluation(Integer evalId, String comment);

    List<Evaluation> getEvaluationHistory(Integer empId);

    Evaluation getEvaluationById(Integer evalId);

    EvaluationEvidence uploadEvidence(Integer evalId, Integer kpiId, String fileUrl, String description);

    EvaluationEvidence verifyEvidence(Integer evidenceId, Boolean isValid, Integer verifiedBy);

    // === Manager ===
    List<Evaluation> getEvaluationsByManagerAndStatus(Integer managerId, EvaluationStatus status);

    void submitManagerScore(Integer evalId, Integer kpiId, BigDecimal managerScore);

    BigDecimal calculateTotalScore(Integer evalId);

    String calculateClassification(BigDecimal totalScore);

    Evaluation approveEvaluation(Integer evalId, String managerComment);

    Evaluation rejectEvaluation(Integer evalId, String managerComment);

    // === Report ===
    List<Evaluation> getEmployeeEvaluations(Integer empId);

    List<Evaluation> getEvaluationsByCycle(Integer cycleId);
}