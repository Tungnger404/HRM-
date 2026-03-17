package com.example.hrm.controller;

import com.example.hrm.service.PayrollInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hr/inquiries")
public class HrInquiryController {

    private final PayrollInquiryService payrollInquiryService;

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) String status,
                       Model model) {
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("inquiries", payrollInquiryService.listInquiriesForHr(status));
        return "hr/inquiry-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("inq", payrollInquiryService.getInquiry(id));
        return "hr/inquiry-detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Integer id,
                          @RequestParam("answer") String answer,
                          RedirectAttributes ra) {
        try {
            payrollInquiryService.resolveInquiryByHr(id, answer);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã phản hồi inquiry thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/hr/inquiries/" + id;
    }
}