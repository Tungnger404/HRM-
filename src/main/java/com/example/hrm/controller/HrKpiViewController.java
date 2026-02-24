package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.FileValidationService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/hr/kpi")
public class HrKpiViewController {

    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final DocumentStorageService documentStorageService;
    private final FileValidationService fileValidationService;
    private final NotificationService notificationService;
    private final KpiEvidenceService kpiEvidenceService;

    public HrKpiViewController(KpiAssignmentRepository kpiAssignmentRepository,
                              DocumentStorageService documentStorageService,
                              FileValidationService fileValidationService,
                              NotificationService notificationService,
                              KpiEvidenceService kpiEvidenceService) {
        this.kpiAssignmentRepository = kpiAssignmentRepository;
        this.documentStorageService = documentStorageService;
        this.fileValidationService = fileValidationService;
        this.notificationService = notificationService;
        this.kpiEvidenceService = kpiEvidenceService;
    }

    @GetMapping("/configure")
    public String showConfigurePage(Model model,
                                   @ModelAttribute("msg") String msg,
                                   @ModelAttribute("err") String err) {
        return "hr/kpi_configure";
    }

    @PostMapping("/configure/upload")
    public String uploadKpiTemplate(
            @RequestParam Integer cycleId,
            @RequestParam(required = false) Integer empId,
            @RequestParam(required = false) Integer deptId,
            @RequestParam String hrComment,
            @RequestParam MultipartFile excelFile,
            RedirectAttributes ra) {
        
        try {
            if (excelFile.isEmpty()) {
                ra.addFlashAttribute("err", "Please select an Excel file");
                return "redirect:/hr/kpi/configure";
            }

            if (!fileValidationService.isValidExcelFile(excelFile)) {
                ra.addFlashAttribute("err", "Invalid file. Only .xlsx or .xls accepted");
                return "redirect:/hr/kpi/configure";
            }

            if (!fileValidationService.validateFileSize(excelFile, 10)) {
                ra.addFlashAttribute("err", "File exceeds 10MB");
                return "redirect:/hr/kpi/configure";
            }

            String storedPath = documentStorageService.store(excelFile);

            if (empId != null) {
                KpiAssignment assignment = new KpiAssignment();
                assignment.setCycleId(cycleId);
                assignment.setEmpId(empId);
                assignment.setHrExcelTemplatePath(storedPath);
                assignment.setHrComment(hrComment);
                assignment.setStatus(KpiAssignment.AssignmentStatus.ASSIGNED);
                assignment.setAssignedAt(LocalDateTime.now());
                
                kpiAssignmentRepository.save(assignment);
                
                notificationService.createKpiAssignmentNotification(empId, assignment.getAssignmentId(), hrComment);
                
                ra.addFlashAttribute("msg", "KPI template sent successfully to Employee ID: " + empId);
            }

            return "redirect:/hr/kpi/configure";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/configure";
        }
    }

    @GetMapping("/pending-verification")
    public String showPendingVerification(Model model,
                                         @ModelAttribute("msg") String msg,
                                         @ModelAttribute("err") String err) {
        List<KpiAssignment> pending = kpiAssignmentRepository
                .findByStatusIn(List.of(
                        KpiAssignment.AssignmentStatus.EMPLOYEE_SUBMITTED,
                        KpiAssignment.AssignmentStatus.EMPLOYEE_RESUBMITTED
                ));
        
        model.addAttribute("assignments", pending);
        return "hr/kpi_pending_verification";
    }

    @GetMapping("/verify/{assignmentId}")
    public String showVerifyPage(@PathVariable Integer assignmentId, Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
        
        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignmentId);
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("evidences", evidences);
        return "hr/kpi_verify";
    }

    @PostMapping("/verify/{assignmentId}/approve")
    public String approveKpi(@PathVariable Integer assignmentId,
                            @RequestParam(required = false) String hrNote,
                            RedirectAttributes ra) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.HR_VERIFIED);
            assignment.setHrVerifiedAt(LocalDateTime.now());
            assignment.setHrVerificationNote(hrNote);
            kpiAssignmentRepository.save(assignment);
            
            notificationService.createEvaluationPendingNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    "Your KPI has been verified by HR"
            );
            
            ra.addFlashAttribute("msg", "Verified and forwarded to Manager");
            return "redirect:/hr/kpi/pending-verification";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/verify/" + assignmentId;
        }
    }

    @PostMapping("/verify/{assignmentId}/reject")
    public String rejectKpi(@PathVariable Integer assignmentId,
                           @RequestParam String rejectReason,
                           RedirectAttributes ra) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.HR_REJECTED);
            assignment.setHrVerificationNote(rejectReason);
            kpiAssignmentRepository.save(assignment);
            
            notificationService.createKpiRejectedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    rejectReason
            );
            
            ra.addFlashAttribute("msg", "Returned to Employee for revision");
            return "redirect:/hr/kpi/pending-verification";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/verify/" + assignmentId;
        }
    }

    @GetMapping("/download-template/{assignmentId}")
    public ResponseEntity<Resource> downloadHrTemplate(@PathVariable Integer assignmentId) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        Resource resource = documentStorageService.loadAsResource(assignment.getHrExcelTemplatePath());
        
        String filename = "KPI_Template.xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    @GetMapping("/download-employee/{assignmentId}")
    public ResponseEntity<Resource> downloadEmployeeSubmission(@PathVariable Integer assignmentId) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        if (assignment.getEmployeeExcelPath() == null) {
            throw new RuntimeException("Employee has not submitted file yet");
        }
        
        Resource resource = documentStorageService.loadAsResource(assignment.getEmployeeExcelPath());
        
        String filename = "KPI_Employee_" + assignment.getEmpId() + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    @GetMapping("/download-evidence/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable Integer evidenceId) {
        KpiEvidence evidence = kpiEvidenceService.getEvidencesByAssignment(0).stream()
                .filter(e -> e.getEvidenceId().equals(evidenceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
        
        Resource resource = documentStorageService.loadAsResource(evidence.getStoredPath());
        
        String filename = evidence.getFileName();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }
}
