package com.example.hrm.service;

import com.example.hrm.dto.EvaluationResultDTO;
import com.example.hrm.entity.Evaluation;
import com.example.hrm.entity.Evaluation.EvaluationStatus;
import com.example.hrm.entity.EvaluationEvidence;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // === Integration with Payroll Module ===
    /**
     * Get evaluation result for an employee in a specific cycle
     * Used by Payroll module to calculate performance bonus
     * 
     * @param employeeId Employee ID
     * @param cycleId Evaluation Cycle ID
     * @return EvaluationResultDTO with classification and suggested bonus percentage
     */
    EvaluationResultDTO getEvaluationResultForPayroll(Integer employeeId, Integer cycleId);

    /**
     * Get evaluation result for an employee in a specific period (month/year)
     * Used by Payroll module when cycle information is not available
     * 
     * @param employeeId Employee ID
     * @param year Year
     * @param month Month (1-12)
     * @return EvaluationResultDTO with classification and suggested bonus percentage
     */
    EvaluationResultDTO getEvaluationResultByPeriod(Integer employeeId, Integer year, Integer month);

    /**
     * Get latest completed evaluation result for an employee
     * Fallback method when cycle/period is unknown
     * 
     * @param employeeId Employee ID
     * @return EvaluationResultDTO or null if no completed evaluation found
     */
    EvaluationResultDTO getLatestEvaluationResult(Integer employeeId);
}