package com.example.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionReviewDTO {

    private EmployeeInfo employee;
    private List<EvaluationRow> evaluations;
    private Double avgScore;
    private Integer evaluationCount;
    private boolean hasEvaluations;
    private String emptyMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private Integer empId;
        private String fullName;
        private String department;
        private String jobTitle;
        private String directManager;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluationRow {
        private Integer cycleId;
        private Integer managerScore;
        private Integer finalScore;
        private String classification;
        private String status;
        private LocalDateTime reviewedAt;
    }
}

