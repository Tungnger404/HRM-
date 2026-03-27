package com.example.hrm.service;

import com.example.hrm.entity.PerformanceRanking;
import com.example.hrm.entity.TrainingRecommendation;

import java.util.List;

public interface PerformanceRankingService {

    // === Ranking Calculation ===
    
    /**
     * Automatically calculate rankings for all employees in a cycle
     * @param cycleId evaluation cycle ID
     */
    void calculateRankingsForCycle(Integer cycleId);

    /**
     * Mark promotion eligibility for employees
     * @param cycleId evaluation cycle ID
     */
    void markPromotionEligibility(Integer cycleId);

    /**
     * Get top performers
     */
    List<PerformanceRanking> getTopPerformers(Integer cycleId, Integer limit);

    /**
     * Get promotion candidates
     */
    List<PerformanceRanking> getPromotionCandidates(Integer cycleId);

    /**
     * Get ranking of an employee in a cycle
     */
    PerformanceRanking getRankingByEmployeeAndCycle(Integer empId, Integer cycleId);

    // === Auto Training Recommendation ===
    
    /**
     * Automatically create training recommendations from evaluation results
     * - Classification C/D: create HIGH priority recommendations + notify manager
     * - Classification B: create MEDIUM priority recommendations + notify employee
     * - Classification A: do not create recommendations (employee can self-enroll)
     * 
     * @param evalId approved evaluation ID
     * @return list of created recommendations
     */
    List<TrainingRecommendation> autoCreateTrainingRecommendations(Integer evalId);

    /**
     * Analyze weak KPI areas and match training programs
     * @param evalId evaluation ID
     * @return list of recommendations based on weak KPI areas
     */
    List<TrainingRecommendation> analyzeWeakKPIsAndRecommend(Integer evalId);
}
