package com.example.hrm.controller;

import com.example.hrm.entity.TrainingProgram;
import com.example.hrm.repository.TrainingProgramRepository;
import com.example.hrm.service.CurrentEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller for Training web pages (Thymeleaf views)
 * Using mock data in templates - no service dependencies needed yet
 */
@Controller
@RequestMapping("/training")
public class TrainingViewController {

    @Autowired
    private CurrentEmployeeService currentEmployeeService;

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;

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
        return "redirect:/employee/training/my-training";
    }

    /**
     * Register for training program (Pure Server-Side - giống team)
     */
    @PostMapping("/programs/{programId}/register")
    public String registerForTraining(
            Principal principal,
            @PathVariable Integer programId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);

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
    public String showProgramDetails(@PathVariable Integer programId, Model model, RedirectAttributes ra) {
        TrainingProgram program = trainingProgramRepository.findById(programId).orElse(null);

        if (program == null) {
            ra.addFlashAttribute("err", "Training program not found");
            return "redirect:/training/programs";
        }

        model.addAttribute("program", program);
        model.addAttribute("programId", programId);
        model.addAttribute("pageTitle", "Training Program Details");
        return "training/program-details";
    }

    /**
     * Show employee's training progress
     */
    @GetMapping("/my-progress")
    public String showMyTrainingProgress(Principal principal, Model model) {
        return "redirect:/employee/training/my-training";
    }

    /**
     * Show evidence submission form
     */
    @GetMapping("/submit-evidence/{progressId}")
    public String showEvidenceSubmissionForm(@PathVariable Integer progressId, Model model) {
        try {
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
    public String showTrainingRecommendations() {
        // Keep old route working for existing notifications; page moved to employee training module.
        return "redirect:/employee/training/my-training";
    }
}
