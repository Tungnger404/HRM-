package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.TrainingService;
import com.example.hrm.entity.Department;
import com.example.hrm.entity.EvalCycle;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EvalCycleRepository;
import com.example.hrm.repository.JobPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.OptionalDouble;

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

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private CurrentEmployeeService currentEmployeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private EvalCycleRepository evalCycleRepository;

    @GetMapping("/pending")
    public String showPendingEvaluations(Principal principal,
                                         Model model,
                                         @ModelAttribute("msg") String msg,
                                         @ModelAttribute("err") String err) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        List<KpiAssignment> allPending = kpiAssignmentRepository
                .findByStatusOrderByEmployeeSubmittedAtDesc(KpiAssignment.AssignmentStatus.HR_VERIFIED);

        List<KpiAssignment> pending = allPending.stream()
                .filter(a -> teamEmpIds.contains(a.getEmpId()))
                .toList();

        model.addAttribute("assignments", pending);
        return "manager/evaluation_pending";
    }

    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId,
                                         Principal principal,
                                         Model model) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        List<KpiAssignment> cycleAssignments = kpiAssignmentRepository.findByCycleId(cycleId);

        // 1) lọc assignment hợp lệ của team
        List<KpiAssignment> validAssignments = cycleAssignments.stream()
                .filter(a -> teamEmpIds.contains(a.getEmpId()))
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.HR_VERIFIED
                        || a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .toList();

        // 2) gom theo empId => mỗi nhân viên chỉ còn 1 dòng
        Map<Integer, List<KpiAssignment>> groupedByEmp = validAssignments.stream()
                .collect(Collectors.groupingBy(KpiAssignment::getEmpId));

        List<KpiAssignment> rankingAssignments = new ArrayList<>();

        for (Map.Entry<Integer, List<KpiAssignment>> entry : groupedByEmp.entrySet()) {
            Integer empId = entry.getKey();
            List<KpiAssignment> empAssignments = entry.getValue();

            // lấy 1 record đại diện để giữ assignmentId / status / deptId
            KpiAssignment latest = empAssignments.stream()
                    .max(Comparator.comparing(KpiAssignment::getAssignmentId))
                    .orElse(empAssignments.get(0));

            // tính average manager score
            OptionalDouble managerAvgOpt = empAssignments.stream()
                    .filter(a -> a.getManagerScore() != null)
                    .mapToInt(KpiAssignment::getManagerScore)
                    .average();

            // tính average self score
            OptionalDouble selfAvgOpt = empAssignments.stream()
                    .filter(a -> a.getSelfScore() != null)
                    .mapToInt(KpiAssignment::getSelfScore)
                    .average();

            Integer avgManagerScore = managerAvgOpt.isPresent() ? (int) Math.round(managerAvgOpt.getAsDouble()) : null;
            Integer avgSelfScore = selfAvgOpt.isPresent() ? (int) Math.round(selfAvgOpt.getAsDouble()) : null;

            Integer finalScore = avgManagerScore != null ? avgManagerScore : avgSelfScore;

            // tạo 1 object tổng hợp cho 1 employee
            KpiAssignment summary = new KpiAssignment();
            summary.setAssignmentId(latest.getAssignmentId());
            summary.setCycleId(latest.getCycleId());
            summary.setEmpId(empId);
            summary.setDeptId(latest.getDeptId());
            summary.setStatus(latest.getStatus());

            summary.setSelfScore(avgSelfScore);
            summary.setManagerScore(avgManagerScore);

            // classification
            String classification = latest.getClassification();
            if (classification == null || classification.isBlank()) {
                classification = calculateClassification(finalScore);
            }
            summary.setClassification(classification);

            // promotion recommendation: nếu có bất kỳ KPI nào recommend promotion thì coi là true
            boolean recommendPromotion = empAssignments.stream()
                    .anyMatch(a -> Boolean.TRUE.equals(a.getPromotionRecommendation()));
            summary.setPromotionRecommendation(recommendPromotion);

            // training recommendation: lấy recommendation đầu tiên khác rỗng
            String trainingRecommendation = empAssignments.stream()
                    .map(KpiAssignment::getTrainingRecommendation)
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst()
                    .orElse(null);

            // nếu chưa có recommendation mà điểm thấp thì gán mặc định
            if ((trainingRecommendation == null || trainingRecommendation.isBlank())
                    && finalScore != null && finalScore < 75) {
                trainingRecommendation = "Training Required";
            }
            summary.setTrainingRecommendation(trainingRecommendation);

            rankingAssignments.add(summary);
        }

        // 3) sort theo final score
        rankingAssignments = rankingAssignments.stream()
                .sorted(Comparator.comparing(
                        a -> a.getManagerScore() != null ? a.getManagerScore() : a.getSelfScore(),
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

    private String calculateClassification(Integer score) {
        if (score == null) return "D";
        if (score >= 85) return "A";
        if (score >= 75) return "B";
        if (score >= 60) return "C";
        return "D";
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
        Employee employee = employeeRepository.findById(assignment.getEmpId()).orElse(null);

        model.addAttribute("assignment", assignment);
        model.addAttribute("employee", employee);
        model.addAttribute("evidences", evidences);
        model.addAttribute("evaluationId", assignmentId);
        return "manager/evaluation_review";
    }

    @PostMapping("/review/{assignmentId}/approve")
    public String approveEvaluation(
            Principal principal,
            @PathVariable Integer assignmentId,
            @RequestParam Integer managerScore,
            @RequestParam String classification,
            @RequestParam(required = false) String managerComment,
            @RequestParam(defaultValue = "false") boolean recommendPromotion,
            @RequestParam(defaultValue = "false") boolean recommendTraining,
            @RequestParam(required = false) String trainingRecommendation,
            RedirectAttributes ra) {

        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);

            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            assignment.setManagerScore(managerScore);
            assignment.setClassification(classification);
            assignment.setManagerComment(managerComment);
            assignment.setPromotionRecommendation(recommendPromotion);
            assignment.setTrainingRecommendation(recommendTraining ? trainingRecommendation : null);
            assignment.setManagerReviewedAt(LocalDateTime.now());

            assignment.setStatus(KpiAssignment.AssignmentStatus.COMPLETED);
            kpiAssignmentRepository.save(assignment);

            notificationService.createEvaluationCompletedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    managerScore + "/100"
            );

            if (recommendTraining && trainingRecommendation != null && !trainingRecommendation.trim().isEmpty()) {
                trainingService.createRecommendation(
                        assignment.getEmpId(),
                        assignment.getAssignmentId(),
                        null,
                        trainingRecommendation,
                        managerScore < 70 ? "HIGH" : "MEDIUM",
                        currentManager.getEmpId()
                );

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
