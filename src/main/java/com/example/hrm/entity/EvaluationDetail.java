package com.example.hrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "evaluation_kpi_scores")
public class EvaluationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "eval_id", nullable = false)
    private Integer evalId;

    @Column(name = "kpi_id", nullable = false)
    private Integer kpiId;

    @Column(name = "self_score", precision = 5, scale = 2)
    private BigDecimal selfScore;

    @Column(name = "manager_score", precision = 5, scale = 2)
    private BigDecimal managerScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEvalId() { return evalId; }
    public void setEvalId(Integer evalId) { this.evalId = evalId; }

    public Integer getKpiId() { return kpiId; }
    public void setKpiId(Integer kpiId) { this.kpiId = kpiId; }

    public BigDecimal getSelfScore() { return selfScore; }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }

    public BigDecimal getManagerScore() { return managerScore; }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
}
