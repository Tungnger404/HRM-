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
     * Automatically calculate rankings for all employees in the cycle
     * Typically called after the evaluation cycle ends
     */
    @PostMapping("/calculate/{cycleId}")
    public ResponseEntity<String> calculateRankings(@PathVariable Integer cycleId) {
        try {
            performanceRankingService.calculateRankingsForCycle(cycleId);
            return ResponseEntity.ok("Rankings and promotion eligibility refreshed successfully for cycle " + cycleId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating rankings: " + e.getMessage());
        }
    }

    /**
     * GET /api/performance-ranking/cycle/{cycleId}/top?limit=10
     * Get top performers in a cycle
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
     * Get promotion-eligible candidates
     */
    @GetMapping("/cycle/{cycleId}/promotion-candidates")
    public ResponseEntity<List<PerformanceRanking>> getPromotionCandidates(@PathVariable Integer cycleId) {
        List<PerformanceRanking> candidates = performanceRankingService.getPromotionCandidates(cycleId);
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/performance-ranking/employee/{empId}/cycle/{cycleId}
     * Get ranking for one employee in a cycle
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
     * Automatically create training recommendations after evaluation approval
     * 
     * Flow:
     * - Manager approve evaluation
     * - Call this API to create recommendations automatically
     * - If C/D: HIGH priority, notify manager
     * - If B: MEDIUM priority, notify employee
     * - If A: do not create
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
     * Analyze weak KPI areas and suggest suitable courses
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
