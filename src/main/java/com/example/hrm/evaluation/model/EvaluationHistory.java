package com.example.hrm.evaluation.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_history")
public class EvaluationHistory {

    public enum HistoryAction {
        SELF_SUBMIT,
        HR_VERIFY,
        MANAGER_APPROVE,
        MANAGER_EDIT,
        FINALIZED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "eval_id", nullable = false)
    private Integer evalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 50)
    private HistoryAction action;

    @Column(name = "action_by", nullable = false)
    private Integer actionBy;

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Column(name = "old_score", precision = 5, scale = 2)
    private BigDecimal oldScore;

    @Column(name = "new_score", precision = 5, scale = 2)
    private BigDecimal newScore;

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    // Getters and Setters
    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getEvalId() {
        return evalId;
    }

    public void setEvalId(Integer evalId) {
        this.evalId = evalId;
    }

    public HistoryAction getAction() {
        return action;
    }

    public void setAction(HistoryAction action) {
        this.action = action;
    }

    public Integer getActionBy() {
        return actionBy;
    }

    public void setActionBy(Integer actionBy) {
        this.actionBy = actionBy;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }

    public BigDecimal getOldScore() {
        return oldScore;
    }

    public void setOldScore(BigDecimal oldScore) {
        this.oldScore = oldScore;
    }

    public BigDecimal getNewScore() {
        return newScore;
    }

    public void setNewScore(BigDecimal newScore) {
        this.newScore = newScore;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}