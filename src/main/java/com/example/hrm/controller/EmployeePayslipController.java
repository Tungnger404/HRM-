package com.example.hrm.controller;

import com.example.hrm.dto.PayrollInquiryCreateDTO;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
        model.addAttribute("inquiries", payrollService.listInquiriesForEmployee(empId, payslipId));
        model.addAttribute("inq", new PayrollInquiryCreateDTO(payslipId, ""));

        return "employee/payslip-detail";
    }

    @PostMapping("/{payslipId}/inquiry")
    public String submitInquiry(@PathVariable Integer payslipId,
                                @ModelAttribute("inq") @Valid PayrollInquiryCreateDTO req,
                                BindingResult br,
                                Model model,
                                Principal principal,
                                RedirectAttributes ra) {

        Integer empId = currentEmployeeService.requireEmployee(principal).getId();
        req.setPayslipId(payslipId);

        if (br.hasErrors()) {
            // render lại detail + list inquiries + show errors
            model.addAttribute("p", payrollService.getPayslipDetailForEmployee(empId, payslipId));
            model.addAttribute("inquiries", payrollService.listInquiriesForEmployee(empId, payslipId));
            return "employee/payslip-detail";
        }

        payrollService.submitInquiry(empId, req);
        ra.addFlashAttribute("msg", "Đã gửi thắc mắc. Vui lòng chờ quản lý phản hồi.");
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
