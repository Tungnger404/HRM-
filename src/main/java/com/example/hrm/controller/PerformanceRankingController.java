package com.example.hrm.controller;

import com.example.hrm.entity.PerformanceRanking;
import com.example.hrm.entity.TrainingRecommendation;
import com.example.hrm.service.PerformanceRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance-ranking")
public class PerformanceRankingController {

    @Autowired
    private PerformanceRankingService performanceRankingService;

    // === Ranking Calculation ===

    /**
     * POST /api/performance-ranking/calculate/{cycleId}
     * Tự động tính ranking cho tất cả nhân viên trong cycle
     * Thường gọi sau khi kết thúc cycle đánh giá
     */
    @PostMapping("/calculate/{cycleId}")
    public ResponseEntity<String> calculateRankings(@PathVariable Integer cycleId) {
        try {
            performanceRankingService.calculateRankingsForCycle(cycleId);
            performanceRankingService.markPromotionEligibility(cycleId);
            return ResponseEntity.ok("Rankings calculated successfully for cycle " + cycleId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating rankings: " + e.getMessage());
        }
    }

    /**
     * GET /api/performance-ranking/cycle/{cycleId}/top?limit=10
     * Lấy danh sách top performers trong cycle
     */
    @GetMapping("/cycle/{cycleId}/top")
    public ResponseEntity<List<PerformanceRanking>> getTopPerformers(
            @PathVariable Integer cycleId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<PerformanceRanking> topPerformers = performanceRankingService.getTopPerformers(cycleId, limit);
        return ResponseEntity.ok(topPerformers);
    }

    /**
     * GET /api/performance-ranking/cycle/{cycleId}/promotion-candidates
     * Lấy danh sách ứng viên đủ điều kiện thăng chức
     */
    @GetMapping("/cycle/{cycleId}/promotion-candidates")
    public ResponseEntity<List<PerformanceRanking>> getPromotionCandidates(@PathVariable Integer cycleId) {
        List<PerformanceRanking> candidates = performanceRankingService.getPromotionCandidates(cycleId);
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/performance-ranking/employee/{empId}/cycle/{cycleId}
     * Lấy ranking của một nhân viên trong cycle
     */
    @GetMapping("/employee/{empId}/cycle/{cycleId}")
    public ResponseEntity<PerformanceRanking> getRankingByEmployee(
            @PathVariable Integer empId,
            @PathVariable Integer cycleId) {
        PerformanceRanking ranking = performanceRankingService.getRankingByEmployeeAndCycle(empId, cycleId);
        if (ranking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ranking);
    }

    // === Auto Training Recommendation ===

    /**
     * POST /api/performance-ranking/auto-recommend/{evalId}
     * Tự động tạo training recommendation sau khi evaluation được approve
     * 
     * Flow:
     * - Manager approve evaluation
     * - Gọi API này để tạo recommendation tự động
     * - Nếu C/D: priority HIGH, notify Manager
     * - Nếu B: priority MEDIUM, notify Employee
     * - Nếu A: không tạo
     */
    @PostMapping("/auto-recommend/{evalId}")
    public ResponseEntity<List<TrainingRecommendation>> autoRecommendTraining(@PathVariable Integer evalId) {
        try {
            List<TrainingRecommendation> recommendations =
                    performanceRankingService.autoCreateTrainingRecommendations(evalId);
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * POST /api/performance-ranking/analyze-weak-kpi/{evalId}
     * Phân tích KPI yếu và đề xuất khóa học phù hợp
     */
    @PostMapping("/analyze-weak-kpi/{evalId}")
    public ResponseEntity<List<TrainingRecommendation>> analyzeWeakKPIs(@PathVariable Integer evalId) {
        try {
            List<TrainingRecommendation> recommendations =
                    performanceRankingService.analyzeWeakKPIsAndRecommend(evalId);
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }
}
