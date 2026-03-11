package com.example.hrm.controller;

import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollEmployeeService;
import com.example.hrm.service.PayrollExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employee/payslips")
public class EmployeePayslipController {

    private final PayrollEmployeeService payrollEmployeeService;
    private final PayrollExportService payrollExportService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        model.addAttribute("payslips", payrollEmployeeService.listEmployeePayslips(empId));
        return "employee/payslip-list";
    }

    @GetMapping("/{payslipId}")
    public String detail(@PathVariable Integer payslipId, Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        model.addAttribute("p", payrollEmployeeService.getPayslipDetailForEmployee(empId, payslipId));
        return "employee/payslip-detail";
    }

    @GetMapping("/{payslipId}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer payslipId, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        byte[] pdf = payrollExportService.buildPayslipPdf(empId, payslipId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION
                        , "attachment; filename=payslip-" + payslipId + ".pdf")
                .body(pdf);
    }
}