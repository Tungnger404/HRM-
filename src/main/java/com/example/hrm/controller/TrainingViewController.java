package com.example.hrm.controller;

import com.example.hrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showTrainingPrograms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            Model model
    ) {
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Training Programs");
        return "training/programs";
    }

    /**
     * Register for training program (Pure Server-Side - giống team)
     */
    @PostMapping("/programs/{programId}/register")
    public String registerForTraining(
            @PathVariable Integer programId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // TODO: Get current employee ID from security context
            Integer employeeId = 1; // Placeholder

            // TODO: Call service to register
            // trainingService.registerEmployee(programId, employeeId);
            
            redirectAttributes.addFlashAttribute("msg", "Successfully registered for the training program!");
            return "redirect:/training/programs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to register: " + e.getMessage());
            return "redirect:/training/programs";
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
        
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("pageTitle", "My Training Progress");
        return "training/my-progress";
    }

    /**
     * Show evidence submission form
     */
    @GetMapping("/submit-evidence/{progressId}")
    public String showEvidenceSubmissionForm(@PathVariable Integer progressId, Model model) {
        try {
            // TODO: Get progress by ID and check if employee owns this progress
            model.addAttribute("progressId", progressId);
            model.addAttribute("pageTitle", "Submit Training Evidence");
            return "training/submit-evidence";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Training progress not found");
            return "error/404";
        }
    }

    /**
     * Submit training evidence with certificate file (Pure Server-Side - giống team)
     */
    @PostMapping("/submit-evidence/{progressId}/submit")
    public String submitEvidence(
            @PathVariable Integer progressId,
            @RequestParam String certificateType,
            @RequestParam(required = false) String certificateNumber,
            @RequestParam String issueDate,
            @RequestParam(required = false) String notes,
            @RequestParam String keyLearnings,
            @RequestParam("certificateFile") MultipartFile certificateFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Validate file
            if (certificateFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("err", "Please upload a certificate file");
                return "redirect:/training/submit-evidence/" + progressId;
            }

            // Validate file size (max 5MB)
            if (certificateFile.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("err", "File size exceeds 5MB");
                return "redirect:/training/submit-evidence/" + progressId;
            }

            // TODO: Save file to storage and update progress
            // String filePath = fileStorageService.saveFile(certificateFile);
            // trainingService.submitEvidence(progressId, certificateType, certificateNumber, 
            //                                issueDate, notes, keyLearnings, filePath);
            
            redirectAttributes.addFlashAttribute("msg", 
                "Training evidence submitted successfully! Your manager will review it soon.");
            return "redirect:/training/my-progress";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to submit evidence: " + e.getMessage());
            return "redirect:/training/submit-evidence/" + progressId;
        }
    }

    /**
     * Show training recommendations for manager/employee
     */
    @GetMapping("/recommendations")
    public String showTrainingRecommendations(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("pageTitle", "Training Recommendations");
        return "training/recommendations";
    }
}
