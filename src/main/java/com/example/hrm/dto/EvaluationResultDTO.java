package com.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for exposing evaluation results to other modules (e.g., Payroll)
 * Used for calculating performance bonuses based on evaluation classification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResultDTO {
    
    private Integer employeeId;
    private String employeeName;
    private Integer evaluationId;
    private Integer cycleId;
    private String cycleName;
    
    // Scores
    private BigDecimal selfScore;
    private BigDecimal managerScore;
    private BigDecimal totalScore;
    
    // Classification (A/B/C/D)
    private String classification;
    private String classificationLabel; // "Excellent", "Good", "Average", "Poor"
    
    // Status
    private String status; // "PENDING", "COMPLETED"
    private LocalDateTime completedAt;
    
    // For bonus calculation
    private BigDecimal suggestedBonusPercentage; // 0.20 for A, 0.10 for B, etc.
}
