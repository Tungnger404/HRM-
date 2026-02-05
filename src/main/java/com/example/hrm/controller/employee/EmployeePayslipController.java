package com.example.hrm.controller.employee;

import com.example.hrm.dto.PayrollInquiryCreateDTO;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import jakarta.validation.Valid;
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

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        model.addAttribute("payslips", payrollService.listEmployeePayslips(empId));
        return "employee/payslip-list";
    }

    @GetMapping("/{payslipId}")
    public String detail(@PathVariable Integer payslipId, Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        model.addAttribute("p", payrollService.getPayslipDetailForEmployee(empId, payslipId));
        model.addAttribute("inq", new PayrollInquiryCreateDTO(payslipId, ""));
        return "employee/payslip-detail";
    }

    @PostMapping("/{payslipId}/inquiry")
    public String submitInquiry(@PathVariable Integer payslipId,
                                @ModelAttribute("inq") @Valid PayrollInquiryCreateDTO req,
                                Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        req.setPayslipId(payslipId);
        payrollService.submitInquiry(empId, req);
        return "redirect:/employee/payslips/" + payslipId;
    }

    @GetMapping("/{payslipId}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer payslipId, Principal principal) {
        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        byte[] pdf = payrollService.buildPayslipPdf(empId, payslipId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-" + payslipId + ".pdf")
                .body(pdf);
    }
}
