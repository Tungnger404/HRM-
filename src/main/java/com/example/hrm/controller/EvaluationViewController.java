package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.KpiEvidenceRepository;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.FileValidationService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Evaluation web pages (Thymeleaf views)
 * Implements 2-step evaluation submission: KPI + Evidence → Self Review
 */
@Controller
@RequestMapping("/evaluation")
public class EvaluationViewController {

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private KpiEvidenceRepository kpiEvidenceRepository;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private KpiEvidenceService kpiEvidenceService;

    @Autowired
    private NotificationService notificationService;

    /**
     * STEP 1: Show KPI + Evidence submission form
     */
    @GetMapping("/submit-kpi")
    public String showKpiSubmissionForm(Model model,
                                       @ModelAttribute("msg") String msg,
                                       @ModelAttribute("err") String err) {
        Integer employeeId = 1;
        
        Integer cycleId = 1;
        KpiAssignment assignment = kpiAssignmentRepository
                .findByEmpIdAndCycleId(employeeId, cycleId)
                .stream()
                .findFirst()
                .orElse(null);
        
        if (assignment != null) {
            List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignment.getAssignmentId());
            model.addAttribute("assignment", assignment);
            model.addAttribute("existingEvidences", evidences);
        }
        
        model.addAttribute("pageTitle", "Submit KPI & Evidence");
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("cycleId", cycleId);
        
        return "evaluation/submit-kpi";
    }

    /**
     * STEP 1: Handle KPI + Evidence submission (saves as DRAFT)
     */
    @PostMapping("/submit-kpi")
    public String handleKpiSubmission(
            @RequestParam Integer cycleId,
            @RequestParam(required = false) MultipartFile kpiExcelFile,
            @RequestParam(required = false) List<MultipartFile> evidenceFiles,
            @RequestParam String employeeComment,
            RedirectAttributes ra) {
        
        try {
            Integer employeeId = 1;
            
            String validationError = fileValidationService.validateFiles(kpiExcelFile, evidenceFiles);
            if (validationError != null) {
                ra.addFlashAttribute("err", validationError);
                return "redirect:/evaluation/submit-kpi";
            }
            
            KpiAssignment assignment = kpiAssignmentRepository
                    .findByEmpIdAndCycleId(employeeId, cycleId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
            
            if (kpiExcelFile != null && !kpiExcelFile.isEmpty()) {
                String excelPath = documentStorageService.store(kpiExcelFile);
                assignment.setEmployeeExcelPath(excelPath);
            }
            
            assignment.setEmployeeComment(employeeComment);
            assignment.setStatus(KpiAssignment.AssignmentStatus.DRAFT);
            kpiAssignmentRepository.save(assignment);
            
            if (evidenceFiles != null) {
                for (MultipartFile file : evidenceFiles) {
                    if (!file.isEmpty()) {
                        kpiEvidenceService.saveEvidence(assignment.getAssignmentId(), file);
                    }
                }
            }
            
            ra.addFlashAttribute("msg", "KPI & Evidence saved. Continue to Step 2.");
            return "redirect:/evaluation/self-review";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/evaluation/submit-kpi";
        }
    }

    /**
     * STEP 2: Show employee self-evaluation form (loads KPI draft from step 1)
     */
    @GetMapping("/self-review")
    public String showSelfReviewForm(Model model,
                                    @ModelAttribute("msg") String msg,
                                    @ModelAttribute("err") String err) {
        Integer employeeId = 1;
        Integer cycleId = 1;
        
        KpiAssignment assignment = kpiAssignmentRepository
                .findByEmpIdAndCycleId(employeeId, cycleId)
                .stream()
                .findFirst()
                .orElse(null);
        
        if (assignment != null && assignment.getStatus() == KpiAssignment.AssignmentStatus.DRAFT) {
            List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignment.getAssignmentId());
            model.addAttribute("kpiAssignment", assignment);
            model.addAttribute("kpiEvidences", evidences);
            model.addAttribute("hasKpiDraft", true);
        } else {
            model.addAttribute("hasKpiDraft", false);
        }
        
        model.addAttribute("pageTitle", "Self Evaluation");
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("cycleId", cycleId);
        
        return "evaluation/self-review";
    }

    /**
     * STEP 2: Submit complete self-evaluation (finalizes submission)
     */
    @PostMapping("/self-review/submit")
    public String submitSelfReview(
            @RequestParam Integer cycleId,
            @RequestParam String selfAssessment,
            @RequestParam Integer selfScore,
            @RequestParam(required = false) String challenges,
            @RequestParam(required = false) String developmentGoals,
            RedirectAttributes ra
    ) {
        try {
            Integer employeeId = 1;
            
            KpiAssignment assignment = kpiAssignmentRepository
                    .findByEmpIdAndCycleId(employeeId, cycleId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
            
            if (assignment.getStatus() != KpiAssignment.AssignmentStatus.DRAFT) {
                ra.addFlashAttribute("err", "You need to complete Step 1 first");
                return "redirect:/evaluation/submit-kpi";
            }
            
            // Save employee self-assessment data (NEW!)
            assignment.setSelfAssessment(selfAssessment);
            assignment.setSelfScore(selfScore);
            assignment.setChallenges(challenges);
            assignment.setDevelopmentGoals(developmentGoals);
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.EMPLOYEE_SUBMITTED);
            assignment.setEmployeeSubmittedAt(LocalDateTime.now());
            kpiAssignmentRepository.save(assignment);
            
            // Notify HR staff about new KPI submission
            Integer hrStaffId = 2; // TODO: Get actual HR staff ID from assignment or config
            notificationService.createKpiSubmittedNotification(
                    hrStaffId,
                    assignment.getAssignmentId(),
                    "Employee #" + employeeId
            );
            
            ra.addFlashAttribute("msg", "Evaluation submitted successfully! Waiting for HR verification.");
            return "redirect:/evaluation/history";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/evaluation/self-review";
        }
    }

    /**
     * Download HR KPI template
     */
    @GetMapping("/download-template/{assignmentId}")
    public ResponseEntity<Resource> downloadHrTemplate(@PathVariable Integer assignmentId) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId).orElse(null);
            if (assignment == null || assignment.getHrExcelTemplatePath() == null) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = documentStorageService.loadAsResource(assignment.getHrExcelTemplatePath());
            String filename = "KPI_Template_" + assignmentId + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download-evidence/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable Integer evidenceId) {
        try {
            KpiEvidence evidence = kpiEvidenceRepository.findById(evidenceId).orElse(null);
            if (evidence == null || evidence.getStoredPath() == null) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = documentStorageService.loadAsResource(evidence.getStoredPath());
            String filename = evidence.getFileName() != null ? evidence.getFileName() : "evidence_" + evidenceId;
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Show manager review form
     */
    @GetMapping("/manager-review/{evaluationId}")
    public String showManagerReviewForm(@PathVariable Integer evaluationId, Model model) {
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("pageTitle", "Manager Review");
        return "evaluation/manager-review";
    }

    /**
     * Show performance ranking dashboard
     */
    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId, Model model) {
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("pageTitle", "Performance Ranking");
        return "evaluation/ranking";
    }

    /**
     * Show promotion recommendations
     */
    @GetMapping("/promotion-recommendations/{cycleId}")
    public String showPromotionRecommendations(@PathVariable Integer cycleId, Model model) {
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("pageTitle", "Promotion Recommendations");
        return "evaluation/promotion-recommendations";
    }

    /**
     * Show employee's evaluation history
     */
    @GetMapping("/history")
    public String showEvaluationHistory(Model model) {
        // TODO: Get current employee ID from security context
        Integer employeeId = 1; // Placeholder
        
        // TODO: Fetch real data from service when database has data
        // For now, use mock data in template for testing
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("pageTitle", "Evaluation History");
        return "evaluation/history";
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
