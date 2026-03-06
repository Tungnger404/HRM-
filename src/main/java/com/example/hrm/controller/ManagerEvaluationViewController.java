package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/evaluation")
public class ManagerEvaluationViewController {

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private KpiEvidenceService kpiEvidenceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/pending")
    public String showPendingEvaluations(Model model,
                                        @ModelAttribute("msg") String msg,
                                        @ModelAttribute("err") String err) {
        List<KpiAssignment> pending = kpiAssignmentRepository
                .findByStatusOrderByEmployeeSubmittedAtDesc(KpiAssignment.AssignmentStatus.HR_VERIFIED);
        
        model.addAttribute("assignments", pending);
        return "manager/evaluation_pending";
    }

    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId, Model model) {
        List<KpiAssignment> cycleAssignments = kpiAssignmentRepository.findByCycleId(cycleId);

        // Show records already reviewed or ready for manager review
        List<KpiAssignment> rankingAssignments = cycleAssignments.stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.HR_VERIFIED
                        || a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .sorted(Comparator.comparing(
                        KpiAssignment::getManagerScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        Map<Integer, String> empNames = employeeRepository.findAllById(
                        rankingAssignments.stream().map(KpiAssignment::getEmpId).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));

        model.addAttribute("assignments", rankingAssignments);
        model.addAttribute("empNames", empNames);
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("pageTitle", "Performance Ranking");
        return "evaluation/ranking";
    }

    @GetMapping("/review/{assignmentId}")
    public String showReviewPage(@PathVariable Integer assignmentId, Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err,
                                 RedirectAttributes ra) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            ra.addFlashAttribute("err", "Evaluation assignment not found.");
            return "redirect:/manager/evaluation/pending";
        }

        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignmentId);

        model.addAttribute("assignment", assignment);
        model.addAttribute("evidences", evidences);
        model.addAttribute("evaluationId", assignmentId);
        return "evaluation/manager-review";
    }

    @PostMapping("/review/{assignmentId}/approve")
    public String approveEvaluation(
            @PathVariable Integer assignmentId,
            @RequestParam Integer managerScore,
            @RequestParam String classification,
            @RequestParam(required = false) String managerComment,
            @RequestParam(defaultValue = "false") boolean recommendPromotion,
            @RequestParam(defaultValue = "false") boolean recommendTraining,
            @RequestParam(required = false) String trainingRecommendation,
            RedirectAttributes ra) {
        
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
            // Save manager review data (NEW!)
            assignment.setManagerScore(managerScore);
            assignment.setClassification(classification);
            assignment.setManagerComment(managerComment);
            assignment.setPromotionRecommendation(recommendPromotion);
            assignment.setTrainingRecommendation(trainingRecommendation);
            assignment.setManagerReviewedAt(LocalDateTime.now());
            
            assignment.setStatus(KpiAssignment.AssignmentStatus.COMPLETED);
            kpiAssignmentRepository.save(assignment);
            
            notificationService.createEvaluationCompletedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    managerScore + "/100"
            );
            
            if (recommendTraining && trainingRecommendation != null) {
                notificationService.createTrainingRecommendationNotification(
                        assignment.getEmpId(),
                        trainingRecommendation
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
