package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.TrainingAssignment;
import com.example.hrm.entity.TrainingProgram;
import com.example.hrm.repository.TrainingAssignmentRepository;
import com.example.hrm.repository.TrainingProgramRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/training")
@RequiredArgsConstructor
public class EmployeeTrainingController {

    private final TrainingAssignmentRepository assignmentRepository;
    private final TrainingProgramRepository programRepository;
    private final CurrentEmployeeService currentEmployeeService;
    private final DocumentStorageService documentStorageService;

    @GetMapping("/my-training")
    public String showMyTraining(Principal principal, Model model) {
        Employee employee = currentEmployeeService.requireEmployee(principal);
        List<TrainingAssignment> assignments = assignmentRepository.findByEmpId(employee.getEmpId());

        model.addAttribute("assignments", assignments);
        return "employee/my-training";
    }

    @GetMapping("/detail/{id}")
    public String showTrainingDetail(@PathVariable Integer id, Principal principal, Model model, RedirectAttributes ra) {
        try {
            Employee employee = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = assignmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Training assignment not found"));

            if (!assignment.getEmpId().equals(employee.getEmpId())) {
                throw new IllegalArgumentException("Access denied");
            }

            TrainingProgram program = null;
            if (assignment.getProgramId() != null) {
                program = programRepository.findById(assignment.getProgramId()).orElse(null);
            }

            model.addAttribute("assignment", assignment);
            model.addAttribute("program", program);
            return "employee/training-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", e.getMessage());
            return "redirect:/employee/training/my-training";
        }
    }

    @PostMapping("/{id}/start")
    public String startTraining(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        try {
            Employee employee = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = assignmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Training assignment not found"));

            if (!assignment.getEmpId().equals(employee.getEmpId())) {
                throw new IllegalArgumentException("Access denied");
            }

            if (assignment.getStatus() != TrainingAssignment.AssignmentStatus.ASSIGNED) {
                throw new IllegalArgumentException("Training already started");
            }

            assignment.setStatus(TrainingAssignment.AssignmentStatus.IN_PROGRESS);
            assignmentRepository.save(assignment);

            ra.addFlashAttribute("toastSuccess", "Training started successfully!");
            return "redirect:/employee/training/detail/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", e.getMessage());
            return "redirect:/employee/training/my-training";
        }
    }

    @PostMapping("/{id}/submit-evidence")
    public String submitEvidence(
            @PathVariable Integer id,
            @RequestParam String completionNote,
            @RequestParam String completionDate,
            @RequestParam(required = false) String certificateCode,
            @RequestParam(required = false) MultipartFile certificateFile,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Employee employee = currentEmployeeService.requireEmployee(principal);
            TrainingAssignment assignment = assignmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Training assignment not found"));

            if (!assignment.getEmpId().equals(employee.getEmpId())) {
                throw new IllegalArgumentException("Access denied");
            }

            if (assignment.getStatus() != TrainingAssignment.AssignmentStatus.IN_PROGRESS 
                && assignment.getStatus() != TrainingAssignment.AssignmentStatus.REJECTED) {
                throw new IllegalArgumentException("Cannot submit evidence for this training");
            }

            assignment.setCompletionNote(completionNote);
            assignment.setCompletionDate(LocalDate.parse(completionDate));
            assignment.setCertificateCode(certificateCode);
            assignment.setSubmittedAt(LocalDateTime.now());
            assignment.setStatus(TrainingAssignment.AssignmentStatus.PENDING_REVIEW);

            if (certificateFile != null && !certificateFile.isEmpty()) {
                String filePath = documentStorageService.store(certificateFile);
                assignment.setCertificateFilePath(filePath);
            }

            assignmentRepository.save(assignment);

            ra.addFlashAttribute("toastSuccess", "Evidence submitted successfully! Waiting for review.");
            return "redirect:/employee/training/detail/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Error: " + e.getMessage());
            return "redirect:/employee/training/detail/" + id;
        }
    }
}
