package com.example.hrm.controller;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.PerformanceRanking;
import com.example.hrm.entity.Department;
import com.example.hrm.entity.EvalCycle;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.KpiEvidenceRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.PerformanceRankingRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.TrainingService;
import com.example.hrm.service.KpiAssignmentWorkflowService;
import com.example.hrm.service.EvaluationPolicyService;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EvalCycleRepository;
import com.example.hrm.repository.JobPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/evaluation")
public class ManagerEvaluationViewController {

    private static final Logger log = LoggerFactory.getLogger(ManagerEvaluationViewController.class);

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private KpiEvidenceRepository kpiEvidenceRepository;

    @Autowired
    private PerformanceRankingRepository performanceRankingRepository;

    @Autowired
    private KpiEvidenceService kpiEvidenceService;

    @Autowired
    private DocumentStorageService documentStorageService;

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
        .collect(Collectors.toMap(
                Employee::getEmpId,
                e -> (e.getFullName() == null || e.getFullName().isBlank())
                        ? ("Employee #" + e.getEmpId())
                        : e.getFullName()
        ));

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
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        // Fetch ranking from official ranking table.
        List<PerformanceRanking> allRankings = performanceRankingRepository.findByCycleId(cycleId);

        // Keep only rankings of current manager's direct reports.
        List<PerformanceRanking> teamRankings = allRankings.stream()
                .filter(r -> teamEmpIds.contains(r.getEmpId()))
                .collect(Collectors.toList());

        teamRankings.sort(Comparator.comparing(PerformanceRanking::getRankOverall, 
                Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Integer, Employee> employeeById = employeeRepository.findAllById(
                        teamRankings.stream().map(PerformanceRanking::getEmpId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Employee::getEmpId, e -> e));

        Map<Integer, String> empNames = employeeById.values().stream()
                .collect(Collectors.toMap(
                        Employee::getEmpId,
                        e -> (e.getFullName() == null || e.getFullName().isBlank())
                                ? ("Employee #" + e.getEmpId())
                                : e.getFullName(),
                        (left, right) -> left,
                        HashMap::new
                ));

        Map<Integer, String> deptNames = departmentRepository.findAllById(
                        employeeById.values().stream().map(Employee::getDeptId).filter(id -> id != null).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(
                        Department::getDeptId,
                        d -> d.getDeptName() != null ? d.getDeptName() : "-"
                ));

        Map<Integer, String> positionNames = jobPositionRepository.findAllById(
                        employeeById.values().stream().map(Employee::getJobId).filter(id -> id != null).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(
                        JobPosition::getJobId,
                        p -> p.getTitle() != null ? p.getTitle() : "-"
                ));

        // Collectors.toMap does not accept null values; some employees may have no assignment yet.
        Map<Integer, Integer> assignmentIds = new HashMap<>();
        for (PerformanceRanking ranking : teamRankings) {
            assignmentIds.putIfAbsent(
                    ranking.getEmpId(),
                    resolveLatestAssignmentId(ranking.getEmpId(), ranking.getCycleId())
            );
        }

        Map<Integer, String> empDeptNames = employeeById.values().stream().collect(Collectors.toMap(
                Employee::getEmpId,
                e -> e.getDeptId() != null ? deptNames.getOrDefault(e.getDeptId(), "-") : "-"
        ));

        Map<Integer, String> empPositionNames = employeeById.values().stream().collect(Collectors.toMap(
                Employee::getEmpId,
                e -> e.getJobId() != null ? positionNames.getOrDefault(e.getJobId(), "-") : "-"
        ));

        // Count classifications
        long countA = teamRankings.stream().filter(r -> "A".equals(r.getClassification())).count();
        long countB = teamRankings.stream().filter(r -> "B".equals(r.getClassification())).count();
        long countC = teamRankings.stream().filter(r -> "C".equals(r.getClassification())).count();
        long countD = teamRankings.stream().filter(r -> "D".equals(r.getClassification())).count();
        log.info("Manager {} viewing ranking cycle {} with {} team rows", currentManager.getEmpId(), cycleId, teamRankings.size());

        // Get available cycles
        List<EvalCycle> cycles = evalCycleRepository.findAll();

        model.addAttribute("rankings", teamRankings);
        model.addAttribute("empNames", empNames);
        model.addAttribute("empDeptNames", empDeptNames);
        model.addAttribute("empPositionNames", empPositionNames);
        model.addAttribute("assignmentIds", assignmentIds);
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
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        
        // Show ALL rankings (no filter)
        List<PerformanceRanking> allRankings = performanceRankingRepository.findByCycleId(cycleId);
        
        // Show manager's team
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        
        // Show completed assignments in this cycle
        List<KpiAssignment> assignments = kpiAssignmentRepository.findByCycleId(cycleId);
        List<KpiAssignment> completed = assignments.stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                .toList();
        
        // Show all rankings
        allRankings.sort(Comparator.comparing(PerformanceRanking::getRankOverall, 
                Comparator.nullsLast(Comparator.naturalOrder())));
        
        Map<Integer, String> empNames = employeeRepository.findAllById(
                allRankings.stream().map(PerformanceRanking::getEmpId).collect(Collectors.toSet())
        ).stream()
        .collect(Collectors.toMap(
                Employee::getEmpId,
                e -> (e.getFullName() == null || e.getFullName().isBlank())
                        ? ("Employee #" + e.getEmpId())
                        : e.getFullName()
        ));
        
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

    @GetMapping("/review/{assignmentId}")
    public String showReviewPage(@PathVariable Integer assignmentId,
                                 Principal principal,
                                 Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err,
                                 RedirectAttributes ra) {
        KpiAssignment assignment;
        try {
            assignment = requireManagedAssignment(principal, assignmentId);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "Evaluation assignment not found.");
            return "redirect:/manager/evaluation/pending";
        }

        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignment.getAssignmentId());
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
            KpiAssignment assignment = requireManagedAssignment(principal, assignmentId);

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
            Principal principal,
            @PathVariable Integer assignmentId,
            @RequestParam String rejectReason,
            RedirectAttributes ra) {

        try {
            KpiAssignment assignment = requireManagedAssignment(principal, assignmentId);

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

    @GetMapping("/download-employee/{assignmentId}")
    public ResponseEntity<Resource> downloadEmployeeSubmission(@PathVariable Integer assignmentId,
                                                               Principal principal) {
        try {
            KpiAssignment assignment = requireManagedAssignment(principal, assignmentId);
            if (assignment.getEmployeeExcelPath() == null || assignment.getEmployeeExcelPath().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = documentStorageService.loadAsResource(assignment.getEmployeeExcelPath().trim());
            String filename = "KPI_Employee_" + assignment.getEmpId() + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download-evidence/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable Integer evidenceId,
                                                     Principal principal) {
        try {
            KpiEvidence evidence = kpiEvidenceRepository.findById(evidenceId)
                    .orElseThrow(() -> new RuntimeException("Evidence not found"));
            requireManagedAssignment(principal, evidence.getAssignmentId());

            if (evidence.getStoredPath() == null || evidence.getStoredPath().isBlank()) {
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

    private Integer resolveLatestAssignmentId(Integer empId, Integer cycleId) {
        return kpiAssignmentRepository.findByEmpIdAndCycleId(empId, cycleId).stream()
                .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED
                        || a.getStatus() == KpiAssignment.AssignmentStatus.HR_VERIFIED)
                .max(Comparator.comparing(KpiAssignment::getAssignmentId))
                .map(KpiAssignment::getAssignmentId)
                .orElse(null);
    }

    private KpiAssignment requireManagedAssignment(Principal principal, Integer assignmentId) {
        Employee manager = currentEmployeeService.requireEmployee(principal);
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        Employee employee = employeeRepository.findById(assignment.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!manager.getEmpId().equals(employee.getDirectManagerId())) {
            throw new RuntimeException("You can only review evaluations of employees in your team.");
        }
        return assignment;
    }
}
