package com.example.hrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "employee_evaluations")
public class Evaluation {

    // Status flow: SELF_REVIEW -> MANAGER_REVIEW -> HR_APPROVAL -> COMPLETED
    public enum EvaluationStatus {
        SELF_REVIEW,     // Employee is entering self-evaluation
        MANAGER_REVIEW,  // Manager is reviewing
        HR_APPROVAL,     // HR is approving
        COMPLETED        // Evaluation completed
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eval_id")
    private Integer evalId;

    @Column(name = "cycle_id", nullable = false)
    private Integer cycleId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "manager_id")
    private Integer managerId;

    @Column(name = "self_score", precision = 5, scale = 2)
    private BigDecimal selfScore;

    @Column(name = "manager_score", precision = 5, scale = 2)
    private BigDecimal managerScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "classification", length = 10)
    private String classification; // A, B, C, D

    @Column(name = "manager_comment", columnDefinition = "NVARCHAR(MAX)")
    private String managerComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EvaluationStatus status = EvaluationStatus.SELF_REVIEW;

    // Getters and Setters
    public Integer getEvalId() { return evalId; }
    public void setEvalId(Integer evalId) { this.evalId = evalId; }

    public Integer getCycleId() { return cycleId; }
    public void setCycleId(Integer cycleId) { this.cycleId = cycleId; }

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public BigDecimal getSelfScore() { return selfScore; }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }

    public BigDecimal getManagerScore() { return managerScore; }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }

    public EvaluationStatus getStatus() { return status; }
    public void setStatus(EvaluationStatus status) { this.status = status; }
}
