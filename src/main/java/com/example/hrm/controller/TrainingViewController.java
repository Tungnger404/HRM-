package com.example.hrm.controller;

import com.example.hrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Training web pages (Thymeleaf views)
 */
@Controller
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingViewController {

    private final TrainingService trainingService;

    /**
     * Show training program list
     */
    @GetMapping("/programs")
    public String showTrainingPrograms(Model model) {
        try {
            var programs = trainingService.getAllTrainingPrograms();
            model.addAttribute("programs", programs);
            model.addAttribute("pageTitle", "Training Programs");
            return "training/programs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load training programs");
            return "error/500";
        }
    }

    /**
     * Show training program details
     */
    @GetMapping("/programs/{programId}")
    public String showProgramDetails(@PathVariable Integer programId, Model model) {
        try {
            var program = trainingService.getTrainingProgramById(programId);
            model.addAttribute("program", program);
            model.addAttribute("pageTitle", "Training Program Details");
            return "training/program-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Training program not found");
            return "error/404";
        }
    }

    /**
     * Show employee's training progress
     */
    @GetMapping("/my-progress")
    public String showMyTrainingProgress(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        try {
            var progressList = trainingService.getProgressByEmployee(employeeId);
            model.addAttribute("progressList", progressList);
            model.addAttribute("pageTitle", "My Training Progress");
            return "training/my-progress";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load training progress");
            return "error/500";
        }
    }

    /**
     * Show evidence submission form
     */
    @GetMapping("/submit-evidence/{progressId}")
    public String showEvidenceSubmissionForm(@PathVariable Integer progressId, Model model) {
        try {
            // Get progress by ID (need to add this method to service if not exists)
            model.addAttribute("progressId", progressId);
            model.addAttribute("pageTitle", "Submit Training Evidence");
            return "training/submit-evidence";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Training progress not found");
            return "error/404";
        }
    }

    /**
     * Show training recommendations for employee
     */
    @GetMapping("/recommendations")
    public String showTrainingRecommendations(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        try {
            var recommendations = trainingService.getRecommendationsByEmployee(employeeId);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("pageTitle", "Training Recommendations");
            return "training/recommendations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load recommendations");
            return "error/500";
        }
    }
}
