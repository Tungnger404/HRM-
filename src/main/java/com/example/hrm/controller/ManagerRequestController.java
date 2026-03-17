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
@RequestMapping({"/manager/attendance-requests", "/manager/view-request"})
public class ManagerRequestController {

    private final RequestWorkflowService workflowService;
    private final CurrentEmployeeService currentEmployeeService;

    public ManagerRequestController(RequestWorkflowService workflowService,
                                    CurrentEmployeeService currentEmployeeService) {
        this.workflowService = workflowService;
        this.currentEmployeeService = currentEmployeeService;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        Integer managerEmpId = currentEmployeeService.requireCurrentEmpId(principal);
        List<LeaveOrOtRequest> requests = workflowService.managerPendingRequests(managerEmpId);
        model.addAttribute("requests", requests);
        return "manager/attendance-request-list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable("id") Integer requestId,
                          @RequestParam(value = "note", required = false) String note,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            Integer managerEmpId = currentEmployeeService.requireCurrentEmpId(principal);
            workflowService.managerApprove(managerEmpId, requestId, note);
            ra.addFlashAttribute("message", "Approved request #" + requestId);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/view-request";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Integer requestId,
                         @RequestParam(value = "note", required = false) String note,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            Integer managerEmpId = currentEmployeeService.requireCurrentEmpId(principal);
            workflowService.managerReject(managerEmpId, requestId, note);
            ra.addFlashAttribute("message", "Rejected request #" + requestId);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/view-request";
    }
}
