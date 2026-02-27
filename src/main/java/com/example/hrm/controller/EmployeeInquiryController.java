package com.example.hrm.controller;

import com.example.hrm.dto.PayrollInquiryCreateDTO;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employee/inquiries")
public class EmployeeInquiryController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) String status,
                       Model model,
                       Principal principal) {

        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);

        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("inquiries", payrollService.listAllInquiriesForEmployee(empId, status));
        model.addAttribute("payslips", payrollService.listEmployeePayslips(empId));

        model.addAttribute("inq", new PayrollInquiryCreateDTO(null, ""));
        return "employee/inquiry-list";
    }

    @PostMapping
    public String create(@ModelAttribute("inq") @Valid PayrollInquiryCreateDTO req,
                         BindingResult br,
                         @RequestParam(value = "status", required = false) String status,
                         Model model,
                         Principal principal,
                         RedirectAttributes ra) {

        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);

        if (br.hasErrors()) {
            model.addAttribute("status", status == null ? "" : status);
            model.addAttribute("inquiries", payrollService.listAllInquiriesForEmployee(empId, status));
            model.addAttribute("payslips", payrollService.listEmployeePayslips(empId));
            return "employee/inquiry-list";
        }

        payrollService.submitInquiry(empId, req);
        ra.addFlashAttribute("msg", "Đã gửi thắc mắc. Vui lòng chờ quản lý phản hồi.");
        return "redirect:/employee/inquiries";
    }
}