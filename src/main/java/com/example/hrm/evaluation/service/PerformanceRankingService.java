package com.example.hrm.evaluation.service;

import com.example.hrm.evaluation.model.PerformanceRanking;
import com.example.hrm.evaluation.model.TrainingRecommendation;

import java.util.List;

public interface PerformanceRankingService {

    // === Ranking Calculation ===
    
    /**
     * Tự động tính ranking cho tất cả nhân viên trong một cycle
     * @param cycleId ID của chu kỳ đánh giá
     */
    void calculateRankingsForCycle(Integer cycleId);

    /**
     * Xác định nhân viên đủ điều kiện thăng chức
     * @param cycleId ID của chu kỳ đánh giá
     */
    void markPromotionEligibility(Integer cycleId);

    /**
     * Lấy danh sách top performers
     */
    List<PerformanceRanking> getTopPerformers(Integer cycleId, Integer limit);

    /**
     * Lấy danh sách ứng viên thăng chức
     */
    List<PerformanceRanking> getPromotionCandidates(Integer cycleId);

    /**
     * Lấy ranking của một nhân viên trong cycle
     */
    PerformanceRanking getRankingByEmployeeAndCycle(Integer empId, Integer cycleId);

    // === Auto Training Recommendation ===
    
    /**
     * Tự động tạo recommendation đào tạo dựa trên kết quả đánh giá
     * - Classification C/D: Tạo recommendation priority HIGH + notify Manager
     * - Classification B: Tạo recommendation priority MEDIUM + notify Employee
     * - Classification A: Không tạo (nhân viên tự chọn nếu muốn)
     * 
     * @param evalId ID của evaluation vừa được approve
     * @return List các recommendation được tạo
     */
    List<TrainingRecommendation> autoCreateTrainingRecommendations(Integer evalId);

    /**
     * Phân tích KPI yếu và match với training programs
     * @param evalId ID của evaluation
     * @return List recommendation dựa trên KPI yếu
     */
    List<TrainingRecommendation> analyzeWeakKPIsAndRecommend(Integer evalId);
}
