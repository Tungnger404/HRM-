package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/manager/evaluation")
public class ManagerEvaluationViewController {

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private KpiEvidenceService kpiEvidenceService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/pending")
    public String showPendingEvaluations(Model model,
                                        @ModelAttribute("msg") String msg,
                                        @ModelAttribute("err") String err) {
        List<KpiAssignment> pending = kpiAssignmentRepository
                .findByStatusOrderByEmployeeSubmittedAtDesc(KpiAssignment.AssignmentStatus.HR_VERIFIED);
        
        model.addAttribute("assignments", pending);
        return "manager/evaluation_pending";
    }

    @GetMapping("/review/{assignmentId}")
    public String showReviewPage(@PathVariable Integer assignmentId, Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignmentId);
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("evidences", evidences);
        return "manager/evaluation_review";
    }

    @PostMapping("/review/{assignmentId}/approve")
    public String approveEvaluation(
            @PathVariable Integer assignmentId,
            @RequestParam Integer managerScore,
            @RequestParam String classification,
            @RequestParam(required = false) String managerComment,
            @RequestParam(defaultValue = "false") boolean recommendPromotion,
            @RequestParam(defaultValue = "false") boolean recommendTraining,
            RedirectAttributes ra) {
        
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.COMPLETED);
            kpiAssignmentRepository.save(assignment);
            
            notificationService.createEvaluationCompletedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    managerScore + "/100"
            );
            
            if (recommendTraining) {
                notificationService.createTrainingRecommendationNotification(
                        assignment.getEmpId(),
                        "Leadership Development"
                );
            }
            
            ra.addFlashAttribute("msg", "Evaluation completed for Employee ID: " + assignment.getEmpId());
            return "redirect:/manager/evaluation/pending";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/manager/evaluation/review/" + assignmentId;
        }
    }

    @PostMapping("/review/{assignmentId}/reject")
    public String rejectEvaluation(
            @PathVariable Integer assignmentId,
            @RequestParam String rejectReason,
            RedirectAttributes ra) {
        
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.MANAGER_REJECTED);
            kpiAssignmentRepository.save(assignment);
            
            notificationService.createKpiRejectedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    "Manager rejection: " + rejectReason
            );
            
            ra.addFlashAttribute("msg", "Returned to Employee for revision");
            return "redirect:/manager/evaluation/pending";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/manager/evaluation/review/" + assignmentId;
        }
    }
}
