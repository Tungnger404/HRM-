package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.KpiEvidenceRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.FileValidationService;
import com.example.hrm.service.KpiSubmissionValidationService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.KpiAssignmentWorkflowService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.SubmissionValidationResult;
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
import java.security.Principal;
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

    @Autowired
    private CurrentEmployeeService currentEmployeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private KpiAssignmentWorkflowService kpiAssignmentWorkflowService;

    @Autowired
    private KpiSubmissionValidationService kpiSubmissionValidationService;

    /**
     * STEP 1: Show KPI + Evidence submission form
     */
    @GetMapping("/submit-kpi")
    public String showKpiSubmissionForm(Principal principal,
                                       Model model,
                                       @ModelAttribute("msg") String msg,
                                       @ModelAttribute("err") String err) {
        Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);
        
        java.util.List<KpiAssignment> allAssignments = kpiAssignmentRepository
                .findByEmpIdOrderByAssignedAtDesc(employeeId);
        
        KpiAssignment assignment = allAssignments.stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.ASSIGNED 
                          || a.getStatus() == KpiAssignment.AssignmentStatus.DRAFT)
                .findFirst()
                .orElse(null);
        
        Integer cycleId = assignment != null ? assignment.getCycleId() : 1;
        
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
            Principal principal,
            @RequestParam Integer assignmentId,
            @RequestParam(required = false) MultipartFile kpiExcelFile,
            @RequestParam(required = false) List<MultipartFile> evidenceFiles,
            @RequestParam String employeeComment,
            RedirectAttributes ra) {
        
        try {
            Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);
            
            KpiAssignment assignment = kpiAssignmentRepository
                    .findById(assignmentId)
                    .orElse(null);
            
            if (assignment == null || !assignment.getEmpId().equals(employeeId)) {
                ra.addFlashAttribute("err", "Invalid KPI assignment. Please contact HR.");
                return "redirect:/evaluation/submit-kpi";
            }
            
            if (assignment.getStatus() != KpiAssignment.AssignmentStatus.ASSIGNED
                && assignment.getStatus() != KpiAssignment.AssignmentStatus.DRAFT) {
                ra.addFlashAttribute("err", "This KPI assignment cannot be edited in its current status.");
                return "redirect:/evaluation/submit-kpi";
            }
            
            String validationError = fileValidationService.validateFiles(kpiExcelFile, evidenceFiles);
            if (validationError != null) {
                ra.addFlashAttribute("err", validationError);
                return "redirect:/evaluation/submit-kpi";
            }

            SubmissionValidationResult draftValidation = kpiSubmissionValidationService.validateDraftSubmission(
                    assignment,
                    kpiExcelFile,
                    evidenceFiles,
                    employeeComment
            );
            if (!draftValidation.isValid()) {
                ra.addFlashAttribute("err", "Please fix the validation errors before continuing.");
                ra.addFlashAttribute("validationErrors", draftValidation.getErrors());
                return "redirect:/evaluation/submit-kpi";
            }
            
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
    public String showSelfReviewForm(Principal principal,
                                    Model model,
                                    @ModelAttribute("msg") String msg,
                                    @ModelAttribute("err") String err) {
        Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);
        
        KpiAssignment assignment = kpiAssignmentRepository
                .findByEmpIdOrderByAssignedAtDesc(employeeId)
                .stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.DRAFT)
                .findFirst()
                .orElse(null);
        
        Integer cycleId = assignment != null ? assignment.getCycleId() : 1;
        
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
            Principal principal,
            @RequestParam Integer assignmentId,
            @RequestParam String selfAssessment,
            @RequestParam Integer selfScore,
            @RequestParam(required = false) String challenges,
            @RequestParam(required = false) String developmentGoals,
            RedirectAttributes ra
    ) {
        try {
            Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);
            
            KpiAssignment assignment = kpiAssignmentRepository
                    .findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
            
            if (!assignment.getEmpId().equals(employeeId)) {
                ra.addFlashAttribute("err", "Invalid assignment access");
                return "redirect:/evaluation/submit-kpi";
            }
            
            KpiAssignment.AssignmentStatus currentStatus = assignment.getStatus();
            
            if (currentStatus != KpiAssignment.AssignmentStatus.DRAFT 
                && currentStatus != KpiAssignment.AssignmentStatus.HR_REJECTED
                && currentStatus != KpiAssignment.AssignmentStatus.MANAGER_REJECTED) {
                ra.addFlashAttribute("err", "Invalid status for submission");
                return "redirect:/evaluation/submit-kpi";
            }

            SubmissionValidationResult finalValidation = kpiSubmissionValidationService.validateBeforeHrSubmission(
                    assignment,
                    selfAssessment,
                    selfScore
            );
            if (!finalValidation.isValid()) {
                ra.addFlashAttribute("err", "Submission failed pre-HR validation. Please correct the issues and submit again.");
                ra.addFlashAttribute("validationErrors", finalValidation.getErrors());
                return "redirect:/evaluation/self-review";
            }
            
            assignment.setSelfAssessment(selfAssessment);
            assignment.setSelfScore(selfScore);
            assignment.setChallenges(challenges);
            assignment.setDevelopmentGoals(developmentGoals);
            
            if (currentStatus == KpiAssignment.AssignmentStatus.HR_REJECTED 
                || currentStatus == KpiAssignment.AssignmentStatus.MANAGER_REJECTED) {
                assignment.setStatus(KpiAssignment.AssignmentStatus.EMPLOYEE_RESUBMITTED);
            } else {
                assignment.setStatus(KpiAssignment.AssignmentStatus.EMPLOYEE_SUBMITTED);
            }
            
            assignment.setEmployeeSubmittedAt(LocalDateTime.now());
            kpiAssignmentRepository.save(assignment);
            
            List<Employee> hrStaffList = employeeRepository.findHrStaff();
            if (!hrStaffList.isEmpty()) {
                Integer hrStaffId = hrStaffList.getFirst().getEmpId();
                notificationService.createKpiSubmittedNotification(
                        hrStaffId,
                        assignment.getAssignmentId(),
                        "Employee #" + employeeId
                );
            }
            
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
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("pageTitle", "Manager Review");
        return "evaluation/manager-review";
    }

    /**
     * Show performance ranking dashboard
     */
    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId, Model model) {
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("pageTitle", "Performance Ranking");
        return "redirect:/dashboard/employee";
    }

    @GetMapping("/promotion-recommendations/{cycleId}")
    public String showPromotionRecommendations(@PathVariable Integer cycleId, Model model) {
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("pageTitle", "Promotion Recommendations");
        return "evaluation/promotion-recommendations";
    }

    /**
     * Show employee's evaluation history
     */
    @GetMapping("/history")
    public String showEvaluationHistory(Principal principal, Model model) {
        Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);

        List<KpiAssignment> completedEvaluations = kpiAssignmentRepository
                .findByEmpIdAndStatus(employeeId, KpiAssignment.AssignmentStatus.COMPLETED);

        model.addAttribute("evaluations", completedEvaluations);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("pageTitle", "Evaluation History");
        return "evaluation/history";
    }

    /**
     * Show evaluation detail
     */
    @GetMapping("/detail/{assignmentId}")
    public String showEvaluationDetail(@PathVariable Integer assignmentId, 
                                       Principal principal,
                                       Model model,
                                       RedirectAttributes ra) {
        try {
            Integer employeeId = currentEmployeeService.requireCurrentEmpId(principal);
            
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Evaluation not found"));
            
            // Check if this evaluation belongs to the current employee
            if (!assignment.getEmpId().equals(employeeId)) {
                throw new RuntimeException("Access denied - this evaluation does not belong to you");
            }
            
            // Get employee and manager info from employee's direct manager
            Employee employee = employeeRepository.findById(assignment.getEmpId()).orElse(null);
            Employee manager = null;
            if (employee != null && employee.getDirectManagerId() != null) {
                manager = employeeRepository.findById(employee.getDirectManagerId()).orElse(null);
            }
            
            model.addAttribute("assignment", assignment);
            model.addAttribute("employee", employee);
            model.addAttribute("manager", manager);
            model.addAttribute("pageTitle", "Evaluation Detail");
            return "evaluation/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/evaluation/history";
        }
    }

    /**
     * Submit manager review
     */
    @PostMapping("/manager-review/{evaluationId}/submit")
    public String submitManagerReview(
            @PathVariable Integer evaluationId,
            @RequestParam String managerComment,
            @RequestParam Integer managerScore,
            @RequestParam(required = false) boolean promoteRecommendation,
            @RequestParam(required = false) boolean trainingRecommendation,
            @RequestParam(required = false) String trainingRecommendationText,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Integer managerId = currentEmployeeService.requireCurrentEmpId(principal);
            
            KpiAssignment assignment = kpiAssignmentRepository.findById(evaluationId)
                    .orElseThrow(() -> new RuntimeException("KPI Assignment not found"));
            
            // Validate manager score
            if (managerScore < 0 || managerScore > 100) {
                throw new RuntimeException("Manager score must be between 0 and 100");
            }
            
            // Gọi workflow service để lưu manager review
            kpiAssignmentWorkflowService.managerApprove(
                    assignment,
                    managerId,
                    managerScore,
                    managerComment,
                    promoteRecommendation,
                    trainingRecommendation,
                    trainingRecommendationText
            );
            
            redirectAttributes.addFlashAttribute("msg", "Manager review submitted successfully!");
            return "redirect:/evaluation/ranking/" + assignment.getCycleId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to submit review: " + e.getMessage());
            return "redirect:/evaluation/manager-review/" + evaluationId;
        }
    }

    /**
     * Approve promotion recommendation
     */
    @PostMapping("/promotion/{assignmentId}/approve")
    public String approvePromotion(
            @PathVariable Integer assignmentId,
            Principal ignoredPrincipal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI Assignment not found"));
            
            if (!assignment.getPromotionRecommendation()) {
                throw new RuntimeException("This assignment does not have a promotion recommendation");
            }
            
            // TODO: Implement promotion approval logic - tạo PromotionRequest record
            // Tạm thời chỉ lưu flag approved
            
            redirectAttributes.addFlashAttribute("msg", "Promotion approved successfully!");
            return "redirect:/evaluation/promotion-recommendations/1";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", "Failed to approve promotion: " + e.getMessage());
            return "redirect:/evaluation/promotion-recommendations/1";
        }
    }
}
