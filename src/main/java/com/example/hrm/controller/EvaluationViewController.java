package com.example.hrm.controller;

import com.example.hrm.service.EvaluationService;
import com.example.hrm.service.KpiService;
import com.example.hrm.service.PerformanceRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Evaluation & Training web pages (Thymeleaf views)
 */
@Controller
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class EvaluationViewController {

    private final EvaluationService evaluationService;
    private final KpiService kpiService;
    private final PerformanceRankingService performanceRankingService;

    /**
     * Show employee self-evaluation form
     */
    @GetMapping("/self-review")
    public String showSelfReviewForm(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        model.addAttribute("pageTitle", "Self Evaluation");
        model.addAttribute("employeeId", employeeId);
        
        return "evaluation/self-review";
    }

    /**
     * Show manager review form
     */
    @GetMapping("/manager-review/{evaluationId}")
    public String showManagerReviewForm(@PathVariable Integer evaluationId, Model model) {
        try {
            var evaluation = evaluationService.getEvaluationById(evaluationId);
            model.addAttribute("evaluation", evaluation);
            model.addAttribute("pageTitle", "Manager Review");
            return "evaluation/manager-review";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Evaluation not found");
            return "error/404";
        }
    }

    /**
     * Show performance ranking dashboard
     */
    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId, Model model) {
        try {
            var rankings = performanceRankingService.getTopPerformers(cycleId, 100); // Get top 100
            model.addAttribute("rankings", rankings);
            model.addAttribute("cycleId", cycleId);
            model.addAttribute("pageTitle", "Performance Ranking");
            return "evaluation/ranking";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load rankings");
            return "error/500";
        }
    }

    /**
     * Show promotion recommendations
     */
    @GetMapping("/promotion-recommendations/{cycleId}")
    public String showPromotionRecommendations(@PathVariable Integer cycleId, Model model) {
        try {
            var recommendations = performanceRankingService.getPromotionCandidates(cycleId);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("cycleId", cycleId);
            model.addAttribute("pageTitle", "Promotion Recommendations");
            return "evaluation/promotion-recommendations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load recommendations");
            return "error/500";
        }
    }

    /**
     * Show employee's evaluation history
     */
    @GetMapping("/history")
    public String showEvaluationHistory(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        try {
            var evaluations = evaluationService.getEmployeeEvaluations(employeeId);
            model.addAttribute("evaluations", evaluations);
            model.addAttribute("pageTitle", "Evaluation History");
            return "evaluation/history";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load evaluation history");
            return "error/500";
        }
    }
}
