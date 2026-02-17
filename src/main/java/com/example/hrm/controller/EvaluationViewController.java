package com.example.hrm.controller;

import com.example.hrm.service.EvaluationService;
import com.example.hrm.service.KpiService;
import com.example.hrm.service.PerformanceRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
     * Submit employee self-evaluation (Pure Server-Side - giống team)
     */
    @PostMapping("/self-review/submit")
    public String submitSelfReview(
            @RequestParam Integer cycleId,
            @RequestParam String selfReview,
            @RequestParam Integer selfScore,
            @RequestParam(required = false) Integer kpiScore_1,
            @RequestParam(required = false) String kpiComment_1,
            @RequestParam(required = false) Integer kpiScore_2,
            @RequestParam(required = false) String kpiComment_2,
            @RequestParam(required = false) Integer kpiScore_3,
            @RequestParam(required = false) String kpiComment_3,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // TODO: Get current employee ID from security context
            Integer employeeId = 1; // Placeholder

            // TODO: Call service to save evaluation
            // evaluationService.createEvaluation(employeeId, cycleId, selfReview, selfScore, kpiScores);
            
            redirectAttributes.addFlashAttribute("msg", "Self evaluation submitted successfully! Waiting for manager review.");
            return "redirect:/evaluation/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to submit evaluation: " + e.getMessage());
            return "redirect:/evaluation/self-review";
        }
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

    /**
     * Submit manager review (Pure Server-Side - giống team)
     */
    @PostMapping("/manager-review/{evaluationId}/submit")
    public String submitManagerReview(
            @PathVariable Integer evaluationId,
            @RequestParam String managerReview,
            @RequestParam Integer managerScore,
            @RequestParam String classification,
            @RequestParam(required = false) boolean promoteRecommendation,
            @RequestParam(required = false) boolean trainingRecommendation,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // TODO: Get current manager ID from security context
            Integer managerId = 1; // Placeholder

            // TODO: Call service to save manager review
            // evaluationService.submitManagerReview(evaluationId, managerId, managerReview, managerScore, classification);
            
            redirectAttributes.addFlashAttribute("msg", "Manager review submitted successfully!");
            return "redirect:/evaluation/ranking/1";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to submit review: " + e.getMessage());
            return "redirect:/evaluation/manager-review/" + evaluationId;
        }
    }

    /**
     * Approve promotion recommendation (Pure Server-Side - giống team)
     */
    @PostMapping("/promotion/{employeeId}/approve")
    public String approvePromotion(
            @PathVariable Integer employeeId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // TODO: Get current manager ID from security context
            Integer managerId = 1; // Placeholder

            // TODO: Call service to approve promotion
            // performanceRankingService.approvePromotion(employeeId, managerId);
            
            redirectAttributes.addFlashAttribute("msg", "Promotion approved successfully!");
            return "redirect:/evaluation/promotion-recommendations/1";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to approve promotion: " + e.getMessage());
            return "redirect:/evaluation/promotion-recommendations/1";
        }
    }
}
