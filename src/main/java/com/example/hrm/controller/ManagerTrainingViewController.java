package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.TrainingAssignment;
import com.example.hrm.entity.TrainingProgram;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.TrainingProgramRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/manager/training")
@RequiredArgsConstructor
public class ManagerTrainingViewController {

    private final TrainingService trainingService;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping("/assign")
    public String showAssignTrainingPage(Principal principal,
                                         Model model,
                                         @RequestParam(required = false) Integer selectedEmpId) {
        Employee currentManager = currentEmployeeService.requireEmployee(principal);
        List<Employee> teamMembers = employeeRepository.findByDirectManagerId(currentManager.getEmpId());
        List<TrainingProgram> programs = trainingProgramRepository.findByStatus(TrainingProgram.TrainingStatus.ACTIVE);

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("programs", programs);
        model.addAttribute("selectedEmpId", selectedEmpId);
        model.addAttribute("pageTitle", "Assign Training");
        return "manager/training-assign";
    }

    @PostMapping("/assign")
    public String assignTraining(@RequestParam Integer empId,
                                 @RequestParam Integer programId,
                                 @RequestParam(required = false) String objective,
                                 Principal principal,
                                 RedirectAttributes ra) {
        try {
            Employee currentManager = currentEmployeeService.requireEmployee(principal);
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new IllegalArgumentException("You can only assign training to employees in your team.");
            }

            String safeObjective = objective == null ? "" : objective.trim();
            if (safeObjective.isBlank()) {
                throw new IllegalArgumentException("Please enter a training objective.");
            }

            TrainingAssignment assignment = trainingService.assignTraining(
                    empId,
                    programId,
                    currentManager.getEmpId(),
                    safeObjective
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
}
