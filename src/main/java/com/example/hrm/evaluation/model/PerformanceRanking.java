package com.example.hrm.evaluation.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_rankings")
public class PerformanceRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id")
    private Integer rankId;

    @Column(name = "cycle_id", nullable = false)
    private Integer cycleId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "rank_overall")
    private Integer rankOverall;

    @Column(name = "rank_in_dept")
    private Integer rankInDept;

    @Column(name = "total_employees_in_dept")
    private Integer totalEmployeesInDept;

    @Column(name = "percentile", precision = 5, scale = 2)
    private BigDecimal percentile;

    @Column(name = "classification", length = 10)
    private String classification; // A, B, C, D

    @Column(name = "is_promotion_eligible")
    private Boolean isPromotionEligible = false;

    @Column(name = "is_training_required")
    private Boolean isTrainingRequired = false;

    @Column(name = "reward_recommendation", columnDefinition = "NVARCHAR(MAX)")
    private String rewardRecommendation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getRankId() {
        return rankId;
    }

    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    public Integer getCycleId() {
        return cycleId;
    }

    public void setCycleId(Integer cycleId) {
        this.cycleId = cycleId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public Integer getRankOverall() {
        return rankOverall;
    }

    public void setRankOverall(Integer rankOverall) {
        this.rankOverall = rankOverall;
    }

    public Integer getRankInDept() {
        return rankInDept;
    }

    public void setRankInDept(Integer rankInDept) {
        this.rankInDept = rankInDept;
    }

    public Integer getTotalEmployeesInDept() {
        return totalEmployeesInDept;
    }

    public void setTotalEmployeesInDept(Integer totalEmployeesInDept) {
        this.totalEmployeesInDept = totalEmployeesInDept;
    }

    public BigDecimal getPercentile() {
        return percentile;
    }

    public void setPercentile(BigDecimal percentile) {
        this.percentile = percentile;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Boolean getIsPromotionEligible() {
        return isPromotionEligible;
    }

    public void setIsPromotionEligible(Boolean isPromotionEligible) {
        this.isPromotionEligible = isPromotionEligible;
    }

    public Boolean getIsTrainingRequired() {
        return isTrainingRequired;
    }

    public void setIsTrainingRequired(Boolean isTrainingRequired) {
        this.isTrainingRequired = isTrainingRequired;
    }

    public String getRewardRecommendation() {
        return rewardRecommendation;
    }

    public void setRewardRecommendation(String rewardRecommendation) {
        this.rewardRecommendation = rewardRecommendation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}