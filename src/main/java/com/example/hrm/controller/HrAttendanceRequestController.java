package com.example.hrm.controller;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.RequestWorkflowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping({"/hr/attendance-requests", "/hr/view-request"})
public class HrAttendanceRequestController {

    private final RequestWorkflowService workflowService;
    private final CurrentEmployeeService currentEmployeeService;

    public HrAttendanceRequestController(RequestWorkflowService workflowService,
                                         CurrentEmployeeService currentEmployeeService) {
        this.workflowService = workflowService;
        this.currentEmployeeService = currentEmployeeService;
    }

    @GetMapping
    public String list(Model model) {
        List<LeaveOrOtRequest> approvedNotProcessed = workflowService.hrApprovedNotProcessed();
        List<LeaveOrOtRequest> managerDecided = workflowService.hrManagerDecidedRequests();

        model.addAttribute("approvedNotProcessed", approvedNotProcessed);
        model.addAttribute("managerDecided", managerDecided);
        return "hr/attendance-request-list";
    }

    @PostMapping("/{id}/process")
    public String process(@PathVariable("id") Integer requestId,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            Integer hrEmpId = currentEmployeeService.requireCurrentEmpId(principal);
            workflowService.hrProcess(hrEmpId, requestId);
            ra.addFlashAttribute("message", "Processed request #" + requestId);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/view-request";
    }
}
