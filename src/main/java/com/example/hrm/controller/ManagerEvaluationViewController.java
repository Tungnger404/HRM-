package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.PerformanceRanking;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.PerformanceRankingRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.TrainingService;
import com.example.hrm.service.KpiAssignmentWorkflowService;
import com.example.hrm.service.EvaluationPolicyService;
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
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.Optional;

@Controller
@RequestMapping("/manager/evaluation")
public class ManagerEvaluationViewController {

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private PerformanceRankingRepository performanceRankingRepository;

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

    @Autowired
    private KpiAssignmentWorkflowService workflowService;

    @Autowired
    private EvaluationPolicyService policyService;

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

    @GetMapping("/history")
    public String showEvaluationHistory(Principal principal,
                                       Model model,
                                       @ModelAttribute("msg") String msg,
                                       @ModelAttribute("err") String err) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        // Get all COMPLETED evaluations for this manager's team
        List<KpiAssignment> completedEvaluations = kpiAssignmentRepository
                .findByStatusOrderByManagerReviewedAtDesc(KpiAssignment.AssignmentStatus.COMPLETED);

        List<KpiAssignment> filtered = completedEvaluations.stream()
                .filter(a -> teamEmpIds.contains(a.getEmpId()))
                .toList();

        // Build employee map for easy lookup
        Map<Integer, String> empNames = employeeRepository.findAllById(
                filtered.stream().map(KpiAssignment::getEmpId).collect(Collectors.toSet())
        ).stream()
        .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));

        model.addAttribute("evaluations", filtered);
        model.addAttribute("empNames", empNames);
        model.addAttribute("pageTitle", "Evaluation History");
        return "manager/evaluation_history";
    }

    @GetMapping("/ranking/{cycleId}")
    public String showPerformanceRanking(@PathVariable Integer cycleId,
                                         Principal principal,
                                         Model model) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        System.out.println("DEBUG: showPerformanceRanking - Manager ID: " + currentManager.getEmpId() + ", CycleId: " + cycleId);
        
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        System.out.println("DEBUG: Team members count: " + teamMembers.size());
        teamMembers.forEach(e -> System.out.println("  - " + e.getEmpId() + " " + e.getFullName()));
        
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        // Fetch từ PerformanceRanking table - đây là dữ liệu ranking chính thức
        List<PerformanceRanking> allRankings = performanceRankingRepository.findByCycleId(cycleId);
        System.out.println("DEBUG: All rankings in cycle " + cycleId + ": " + allRankings.size());
        allRankings.forEach(r -> System.out.println("  - EmpId: " + r.getEmpId() + ", CycleId: " + r.getCycleId() + ", Score: " + r.getFinalScore() + ", Classification: " + r.getClassification()));

        // Lọc chỉ lấy ranking của team members của manager hiện tại
        List<PerformanceRanking> teamRankings = allRankings.stream()
                .filter(r -> teamEmpIds.contains(r.getEmpId()))
                .collect(Collectors.toList());
        System.out.println("DEBUG: Team rankings (filtered): " + teamRankings.size());

        // Sort theo rank_overall (từ 1 tới n)
        teamRankings.sort(Comparator.comparing(PerformanceRanking::getRankOverall, 
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Map empId to employee names
        Map<Integer, String> empNames = employeeRepository.findAllById(
                teamRankings.stream().map(PerformanceRanking::getEmpId).collect(Collectors.toSet())
        ).stream()
        .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));

        // Count classifications
        long countA = teamRankings.stream().filter(r -> "A".equals(r.getClassification())).count();
        long countB = teamRankings.stream().filter(r -> "B".equals(r.getClassification())).count();
        long countC = teamRankings.stream().filter(r -> "C".equals(r.getClassification())).count();
        long countD = teamRankings.stream().filter(r -> "D".equals(r.getClassification())).count();
        System.out.println("DEBUG: Classification counts - A: " + countA + ", B: " + countB + ", C: " + countC + ", D: " + countD);

        // Get available cycles
        List<EvalCycle> cycles = evalCycleRepository.findAll();

        model.addAttribute("rankings", teamRankings);
        model.addAttribute("empNames", empNames);
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("cycles", cycles);
        model.addAttribute("countA", countA);
        model.addAttribute("countB", countB);
        model.addAttribute("countC", countC);
        model.addAttribute("countD", countD);
        model.addAttribute("pageTitle", "Performance Ranking");
        return "evaluation/ranking";
    }

    @GetMapping("/ranking-debug/{cycleId}")
    public String showPerformanceRankingDebug(@PathVariable Integer cycleId,
                                               Principal principal,
                                               Model model) {
        System.out.println("\n=== DEBUG RANKING PAGE ===");
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        System.out.println("Current Manager: " + currentManager.getEmpId() + " - " + currentManager.getFullName());
        
        // Show ALL rankings (no filter)
        List<PerformanceRanking> allRankings = performanceRankingRepository.findByCycleId(cycleId);
        System.out.println("ALL Rankings in cycle " + cycleId + ": " + allRankings.size());
        allRankings.forEach(r -> {
            System.out.println("  - EmpId: " + r.getEmpId() + 
                    ", Score: " + r.getFinalScore() + 
                    ", Classification: " + r.getClassification() + 
                    ", Rank: " + r.getRankOverall());
        });
        
        // Show manager's team
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        System.out.println("Manager's team members: " + teamMembers.size());
        teamMembers.forEach(e -> System.out.println("  - " + e.getEmpId() + " " + e.getFullName()));
        
        // Show completed assignments in this cycle
        List<KpiAssignment> assignments = kpiAssignmentRepository.findByCycleId(cycleId);
        List<KpiAssignment> completed = assignments.stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .toList();
        System.out.println("Completed assignments: " + completed.size());
        completed.forEach(a -> {
            System.out.println("  - EmpId: " + a.getEmpId() + 
                    ", ManagerScore: " + a.getManagerScore() + 
                    ", Classification: " + a.getClassification());
        });
        
        System.out.println("=== END DEBUG ===\n");
        
        // Show all rankings
        allRankings.sort(Comparator.comparing(PerformanceRanking::getRankOverall, 
                Comparator.nullsLast(Comparator.naturalOrder())));
        
        Map<Integer, String> empNames = employeeRepository.findAllById(
                allRankings.stream().map(PerformanceRanking::getEmpId).collect(Collectors.toSet())
        ).stream()
        .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));
        
        long countA = allRankings.stream().filter(r -> "A".equals(r.getClassification())).count();
        long countB = allRankings.stream().filter(r -> "B".equals(r.getClassification())).count();
        long countC = allRankings.stream().filter(r -> "C".equals(r.getClassification())).count();
        long countD = allRankings.stream().filter(r -> "D".equals(r.getClassification())).count();
        
        List<EvalCycle> cycles = evalCycleRepository.findAll();
        
        model.addAttribute("rankings", allRankings);
        model.addAttribute("empNames", empNames);
        model.addAttribute("cycleId", cycleId);
        model.addAttribute("cycles", cycles);
        model.addAttribute("countA", countA);
        model.addAttribute("countB", countB);
        model.addAttribute("countC", countC);
        model.addAttribute("countD", countD);
        model.addAttribute("pageTitle", "Performance Ranking (DEBUG - ALL)");
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
        
        // Nếu không tìm thấy bằng assignmentId, có thể là rankId từ ranking page
        // Thử tìm assignment từ PerformanceRanking
        if (assignment == null) {
            // assignmentId có thể là rankId, cần query khác cách
            Optional<PerformanceRanking> ranking = performanceRankingRepository.findById(assignmentId);
            if (ranking.isPresent()) {
                PerformanceRanking perfRanking = ranking.get();
                // Tìm KpiAssignment mới nhất cho employee này trong cycle này
                List<KpiAssignment> assignments = kpiAssignmentRepository.findByEmpIdAndCycleId(
                        perfRanking.getEmpId(), perfRanking.getCycleId());
                
                if (!assignments.isEmpty()) {
                    // Lấy assignment mới nhất
                    assignment = assignments.stream()
                            .max(Comparator.comparing(KpiAssignment::getAssignmentId))
                            .orElse(null);
                }
            }
        }
        
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
            @RequestParam(required = false) String managerComment,
            @RequestParam(defaultValue = "false") boolean recommendPromotion,
            @RequestParam(defaultValue = "false") boolean recommendTraining,
            @RequestParam(required = false) String trainingRecommendation,
            RedirectAttributes ra) {

        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);

            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            workflowService.managerApprove(
                    assignment,
                    currentManager.getEmpId(),
                    managerScore,
                    managerComment,
                    recommendPromotion,
                    recommendTraining,
                    trainingRecommendation
            );

            ra.addFlashAttribute("toastSuccess", "Evaluation approved successfully!");
            return "redirect:/manager/evaluation/pending";

        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
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
            assignment.setManagerRejectionReason(rejectReason);
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
