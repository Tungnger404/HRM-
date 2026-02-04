package com.example.hrm.evaluation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_recommendations")
public class TrainingRecommendation {

    public enum RecommendationStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        ASSIGNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "eval_id")
    private Integer evalId;

    @Column(name = "program_id", nullable = false)
    private Integer programId;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Column(name = "recommended_by")
    private Integer recommendedBy;

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;

    // Getters and Setters
    public Integer getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Integer recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Integer getEvalId() {
        return evalId;
    }

    public void setEvalId(Integer evalId) {
        this.evalId = evalId;
    }

    public Integer getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public RecommendationStatus getStatus() {
        return status;
    }

    public void setStatus(RecommendationStatus status) {
        this.status = status;
    }

    public Integer getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(Integer recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public LocalDateTime getRecommendedAt() {
        return recommendedAt;
    }

    public void setRecommendedAt(LocalDateTime recommendedAt) {
        this.recommendedAt = recommendedAt;
    }
}