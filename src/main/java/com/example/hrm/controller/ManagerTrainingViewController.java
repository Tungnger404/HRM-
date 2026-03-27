package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.TrainingAssignment;
import com.example.hrm.entity.TrainingProgram;
import com.example.hrm.entity.TrainingRecommendation;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.TrainingAssignmentRepository;
import com.example.hrm.repository.TrainingProgramRepository;
import com.example.hrm.repository.TrainingRecommendationRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/training")
@RequiredArgsConstructor
public class ManagerTrainingViewController {

    private final TrainingService trainingService;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentEmployeeService currentEmployeeService;
    private final TrainingRecommendationRepository recommendationRepository;
    private final TrainingAssignmentRepository assignmentRepository;
    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final NotificationService notificationService;

    @GetMapping("/assign")
    public String showAssignTrainingPage(Principal principal,
                                         Model model,
                                         @RequestParam(required = false) Integer selectedEmpId) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<TrainingProgram> programs = trainingProgramRepository.findByStatus(TrainingProgram.TrainingStatus.ACTIVE);
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).toList();

        List<TrainingAssignment> assignmentHistory = teamEmpIds.isEmpty()
                ? List.of()
                : assignmentRepository.findByEmpIdInOrderByAssignedAtDesc(teamEmpIds);

        Set<Integer> assignedByIds = assignmentHistory.stream()
                .map(TrainingAssignment::getAssignedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, String> assignedByNames = employeeRepository.findAllById(assignedByIds).stream()
                .collect(Collectors.toMap(
                        Employee::getEmpId,
                        e -> e.getFullName() != null ? e.getFullName() : ("Employee #" + e.getEmpId())
                ));

        Map<Integer, String> employeeNames = teamMembers.stream()
                .collect(Collectors.toMap(
                        Employee::getEmpId,
                        e -> e.getFullName() != null ? e.getFullName() : ("Employee #" + e.getEmpId())
                ));

        Set<Integer> programIds = assignmentHistory.stream()
                .map(TrainingAssignment::getProgramId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, String> programNames = trainingProgramRepository.findAllById(programIds).stream()
                .collect(Collectors.toMap(
                        TrainingProgram::getProgramId,
                        p -> p.getProgramName() != null ? p.getProgramName() : ("Program #" + p.getProgramId())
                ));

        Map<Integer, String> assignmentSource = new HashMap<>();
        for (TrainingAssignment assignment : assignmentHistory) {
            assignmentSource.put(
                    assignment.getAssignId(),
                    assignment.getRecommendationId() != null ? "Recommendation" : "Manual"
            );
        }

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("programs", programs);
        model.addAttribute("selectedEmpId", selectedEmpId);
        model.addAttribute("assignmentHistory", assignmentHistory);
        model.addAttribute("employeeNames", employeeNames);
        model.addAttribute("assignedByNames", assignedByNames);
        model.addAttribute("programNames", programNames);
        model.addAttribute("assignmentSource", assignmentSource);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Assign Training");
        return "manager/training-assign";
    }

    @GetMapping("/assign-with-history/{empId}")
    public String showAssignWithHistory(@PathVariable Integer empId,
                                        Principal principal,
                                        Model model,
                                        RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new IllegalArgumentException("You can only assign training to employees in your team.");
            }

            List<KpiAssignment> completedEvaluations = kpiAssignmentRepository
                    .findByEmpIdAndStatusOrderByManagerReviewedAtDesc(empId, KpiAssignment.AssignmentStatus.COMPLETED)
                    .stream()
                    .filter(a -> a.getManagerScore() != null)
                    .toList();

            List<TrainingProgram> programs = trainingProgramRepository.findByStatus(TrainingProgram.TrainingStatus.ACTIVE);

            model.addAttribute("employee", employee);
            model.addAttribute("completedEvaluations", completedEvaluations);
            model.addAttribute("programs", programs);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Assign Training with History");
            return "manager/training-assign-with-history";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/training/assign";
        }
    }

    @GetMapping("/assignments/{id}")
    public String showAssignmentDetail(@PathVariable Integer id,
                                       Principal principal,
                                       Model model,
                                       RedirectAttributes ra) {
        try {
            TrainingAssignment assignment = requireManagedAssignment(principal, id);
            Employee employee = employeeRepository.findById(assignment.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            Employee assignedBy = null;
            if (assignment.getAssignedBy() != null) {
                assignedBy = employeeRepository.findById(assignment.getAssignedBy()).orElse(null);
            }

            TrainingProgram program = null;
            if (assignment.getProgramId() != null) {
                program = trainingProgramRepository.findById(assignment.getProgramId()).orElse(null);
            }

            model.addAttribute("assignment", assignment);
            model.addAttribute("employee", employee);
            model.addAttribute("assignedBy", assignedBy);
            model.addAttribute("program", program);
            model.addAttribute("source", assignment.getRecommendationId() != null ? "Recommendation" : "Manual");
            model.addAttribute("canCancel", assignment.getStatus() != TrainingAssignment.AssignmentStatus.COMPLETED
                    && assignment.getStatus() != TrainingAssignment.AssignmentStatus.CANCELLED);
            model.addAttribute("pageTitle", "Training Assignment Detail");
            return "manager/training-assignment-detail";
        } catch (Exception ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
            return "redirect:/manager/training/assign";
        }
    }

    @PostMapping("/assignments/{id}/cancel")
    public String cancelAssignment(@PathVariable Integer id,
                                   Principal principal,
                                   RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = requireManagedAssignment(principal, id);

            if (assignment.getStatus() == TrainingAssignment.AssignmentStatus.COMPLETED) {
                throw new IllegalArgumentException("Completed assignments cannot be cancelled.");
            }
            if (assignment.getStatus() == TrainingAssignment.AssignmentStatus.CANCELLED) {
                throw new IllegalArgumentException("Assignment is already cancelled.");
            }

            assignment.setStatus(TrainingAssignment.AssignmentStatus.CANCELLED);
            assignment.setReviewedAt(LocalDateTime.now());
            assignment.setReviewedBy(currentManager.getEmpId());
            assignmentRepository.save(assignment);

            ra.addFlashAttribute("msg", "Assignment cancelled successfully.");
            return "redirect:/manager/training/assign?tab=history";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/training/assign?tab=history";
        }
    }

    @PostMapping("/assign")
    public String assignTraining(@RequestParam Integer empId,
                                 @RequestParam Integer programId,
                                 @RequestParam String deadline,
                                 @RequestParam(required = false) String objective,
                                 Principal principal,
                                 RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

            // Security check: employee must belong to current manager's team
            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new IllegalArgumentException("You can only assign training to employees in your team.");
            }

            String safeObjective = objective == null ? "" : objective.trim();
            if (safeObjective.isBlank()) {
                throw new IllegalArgumentException("Please enter a training objective.");
            }

            if (deadline == null || deadline.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select a deadline.");
            }

            LocalDate dueDate = LocalDate.parse(deadline);
            if (dueDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Deadline cannot be before today.");
            }

            TrainingAssignment assignment = trainingService.assignTraining(
                    empId,
                    programId,
                    currentManager.getEmpId(),
                    safeObjective,
                    null,
                    dueDate
            );

            String courseName = trainingProgramRepository.findById(programId)
                    .map(TrainingProgram::getProgramName)
                    .orElse("assigned training");
            notificationService.create(
                    empId,
                    "TRAINING_ASSIGNED",
                    "New training assigned",
                    "You have been assigned training: " + courseName,
                    "/employee/training/detail/" + assignment.getAssignId()
            );

            ra.addFlashAttribute(
                    "msg",
                    "Assigned training #" + assignment.getAssignId() + " to " + employee.getFullName() + "."
            );
            return "redirect:/manager/training/assign?selectedEmpId=" + empId;
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/training/assign";
        }
    }

    @GetMapping("/recommendations")
    public String showRecommendations(Principal principal, Model model) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).collect(Collectors.toList());

        List<TrainingRecommendation> recommendations = recommendationRepository
                .findByStatus(TrainingRecommendation.RecommendationStatus.RECOMMENDED)
                .stream()
                .filter(r -> teamEmpIds.contains(r.getEmpId()))
                .collect(Collectors.toList());

        List<TrainingProgram> programs = trainingProgramRepository.findByStatus(TrainingProgram.TrainingStatus.ACTIVE);

        model.addAttribute("recommendations", recommendations);
        model.addAttribute("programs", programs);
        model.addAttribute("teamMembers", teamMembers);
        return "manager/training-recommendations";
    }

    @PostMapping("/recommendations/{id}/assign")
    public String assignRecommendation(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer programId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String trainingType,
            @RequestParam(required = false) String onlineProgramName,
            @RequestParam(required = false) String onlineProgramUrl,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            TrainingRecommendation recommendation = recommendationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));

            // Security check: employee must belong to current manager's team
            Employee employee = employeeRepository.findById(recommendation.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new IllegalArgumentException("You can only manage training for employees in your team.");
            }

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDate today = LocalDate.now();

            if (start.isBefore(today)) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("End date must be after start date");
            }

            String objective = recommendation.getReason() != null
                    ? recommendation.getReason()
                    : "Based on evaluation recommendation";
            TrainingAssignment assignment;

            if ("OFFLINE".equals(trainingType) && programId != null) {
                TrainingProgram program = trainingProgramRepository.findById(programId)
                        .orElseThrow(() -> new IllegalArgumentException("Training program not found"));
                assignment = trainingService.createManagerAssignment(
                        recommendation.getEmpId(),
                        programId,
                        program.getProgramName(),
                        program.getCourseUrl(),
                        currentManager.getEmpId(),
                        objective,
                        start,
                        end,
                        "OFFLINE",
                        recommendation.getRecommendationId()
                );
            } else if ("ONLINE".equals(trainingType)) {
                if (onlineProgramName == null || onlineProgramName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Please provide program name for online training");
                }
                if (onlineProgramUrl == null || onlineProgramUrl.trim().isEmpty()) {
                    throw new IllegalArgumentException("Please provide course URL for online training");
                }
                assignment = trainingService.createManagerAssignment(
                        recommendation.getEmpId(),
                        null,
                        onlineProgramName.trim(),
                        onlineProgramUrl.trim(),
                        currentManager.getEmpId(),
                        objective,
                        start,
                        end,
                        "ONLINE",
                        recommendation.getRecommendationId()
                );
            } else {
                throw new IllegalArgumentException("Please select valid training type");
            }

            recommendation.setStatus(TrainingRecommendation.RecommendationStatus.ASSIGNED);
            recommendationRepository.save(recommendation);


            notificationService.create(
                    assignment.getEmpId(),
                    "TRAINING_ASSIGNED",
                    "New training assigned",
                    "You have been assigned training: " + (assignment.getProgramName() != null ? assignment.getProgramName() : "assigned training"),
                    "/employee/training/detail/" + assignment.getAssignId()
            );

            ra.addFlashAttribute("toastSuccess", "Training assigned successfully!");
            return "redirect:/manager/training/recommendations";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/manager/training/recommendations";
        }
    }

    @PostMapping("/recommendations/{id}/dismiss")
    public String dismissRecommendation(@PathVariable Integer id, 
                                       Principal principal,
                                       RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            TrainingRecommendation recommendation = recommendationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));

            // Security check: employee must belong to current manager's team
            Employee employee = employeeRepository.findById(recommendation.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new IllegalArgumentException("You can only manage training for employees in your team.");
            }

            recommendation.setStatus(TrainingRecommendation.RecommendationStatus.DISMISSED);
            recommendationRepository.save(recommendation);

            ra.addFlashAttribute("toastSuccess", "Recommendation dismissed!");
            return "redirect:/manager/training/recommendations";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/manager/training/recommendations";
        }
    }

    @GetMapping("/evidence-confirmations")
    public String showEvidenceConfirmations(Principal principal, Model model) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<Integer> teamEmpIds = teamMembers.stream().map(Employee::getEmpId).collect(Collectors.toList());

        List<TrainingAssignment> submissions = assignmentRepository
                .findByStatus(TrainingAssignment.AssignmentStatus.PENDING_REVIEW)
                .stream()
                .filter(a -> teamEmpIds.contains(a.getEmpId()))
                .collect(Collectors.toList());

        Map<Integer, String> empNames = teamMembers.stream()
                .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));

        model.addAttribute("submissions", submissions);
        model.addAttribute("empNames", empNames);
        model.addAttribute("pageTitle", "Training Evidence Confirmations");
        return "manager/training-review";
    }

    @GetMapping("/evidence-confirmations/{id}")
    public String showEvidenceConfirmationDetail(@PathVariable Integer id,
                                                 Principal principal,
                                                 Model model,
                                                 RedirectAttributes ra) {
        try {
            TrainingAssignment assignment = requireManagedAssignment(principal, id);
            Employee employee = employeeRepository.findById(assignment.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            model.addAttribute("assignment", assignment);
            model.addAttribute("employee", employee);
            model.addAttribute("pageTitle", "Training Evidence Detail");
            return "manager/training-review-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/manager/training/evidence-confirmations";
        }
    }

    // Backward-compatible route for existing links/bookmarks.
    @GetMapping("/review-submissions")
    public String redirectLegacyReviewSubmissions() {
        return "redirect:/manager/training/evidence-confirmations";
    }

    @PostMapping("/review/{id}/approve")
    public String approveSubmission(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = requireManagedAssignment(principal, id);

            if (assignment.getStatus() != TrainingAssignment.AssignmentStatus.PENDING_REVIEW) {
                throw new IllegalArgumentException("Only pending evidence can be approved.");
            }

            Employee employee = employeeRepository.findById(assignment.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            assignment.setStatus(TrainingAssignment.AssignmentStatus.COMPLETED);
            assignment.setReviewedBy(currentManager.getEmpId());
            assignment.setReviewedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);

            notificationService.create(
                    assignment.getEmpId(),
                    "TRAINING_APPROVED",
                    "Training evidence approved",
                    "Your training evidence for \"" + (assignment.getProgramName() != null ? assignment.getProgramName() : "assigned training") + "\" has been approved.",
                    "/employee/training/detail/" + assignment.getAssignId()
            );

            if (assignment.getRecommendationId() != null) {
                recommendationRepository.findById(assignment.getRecommendationId()).ifPresent(rec -> {
                    rec.setStatus(TrainingRecommendation.RecommendationStatus.COMPLETED);
                    recommendationRepository.save(rec);
                });
            }

            ra.addFlashAttribute("toastSuccess", "Training completion approved!");
            return "redirect:/manager/training/evidence-confirmations";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/manager/training/evidence-confirmations/" + id;
        }
    }

    @PostMapping("/review/{id}/reject")
    public String rejectSubmission(
            @PathVariable Integer id,
            @RequestParam String reviewComment,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = requireManagedAssignment(principal, id);

            if (assignment.getStatus() != TrainingAssignment.AssignmentStatus.PENDING_REVIEW) {
                throw new IllegalArgumentException("Only pending evidence can be rejected.");
            }

            if (reviewComment == null || reviewComment.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter a rejection reason.");
            }

            Employee employee = employeeRepository.findById(assignment.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            assignment.setStatus(TrainingAssignment.AssignmentStatus.REJECTED);
            assignment.setReviewedBy(currentManager.getEmpId());
            assignment.setReviewedAt(LocalDateTime.now());
            assignment.setReviewComment(reviewComment.trim());
            assignmentRepository.save(assignment);

            notificationService.create(
                    assignment.getEmpId(),
                    "TRAINING_REJECTED",
                    "Training evidence needs revision",
                    "Your training evidence was rejected: " + reviewComment.trim() + ". Please update and resubmit.",
                    "/employee/training/detail/" + assignment.getAssignId()
            );

            ra.addFlashAttribute("toastSuccess", "Evidence rejected. Employee can resubmit.");
            return "redirect:/manager/training/evidence-confirmations";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/manager/training/evidence-confirmations/" + id;
        }
    }

    private TrainingAssignment requireManagedAssignment(Principal principal, Integer assignmentId) {
        Employee manager = currentEmployeeService.requireEmployee(principal);
        TrainingAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        Employee employee = employeeRepository.findById(assignment.getEmpId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!manager.getEmpId().equals(employee.getDirectManagerId())) {
            throw new IllegalArgumentException("You can only review training for employees in your team.");
        }
        return assignment;
    }
}
