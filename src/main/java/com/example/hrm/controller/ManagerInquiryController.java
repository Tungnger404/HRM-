package com.example.hrm.controller;

import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/inquiries")
public class ManagerInquiryController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) String status,
                       Model model,
                       Principal principal) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        model.addAttribute("status", status);
        model.addAttribute("inquiries", payrollService.listInquiriesForManager(managerEmpId, status));
        return "manager/inquiry-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("inq", payrollService.getInquiry(id));
        return "manager/inquiry-detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Integer id,
                          @RequestParam("answer") String answer,
                          Principal principal) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        payrollService.resolveInquiry(managerEmpId, id, answer);
        return "redirect:/manager/inquiries?status=OPEN";
    }
}
