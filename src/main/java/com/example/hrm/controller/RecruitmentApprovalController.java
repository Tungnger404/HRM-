package com.example.hrm.controller;

import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.service.RecruitmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hr/recruitment-request")
@RequiredArgsConstructor
public class RecruitmentApprovalController {

    private final RecruitmentRequestService service;

    // ================= VIEW DETAIL (SCREEN 3) =================
    // URL: /hr/recruitment-request/{id}
    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        RecruitmentRequest request = service.getById(id);
        model.addAttribute("request", request);
        return "recruitment-request/detail";

    }

    // ================= APPROVE =================
    // URL: /hr/recruitment-request/{id}/approve
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id) {
        service.approveRequest(id);
        return "redirect:/hr/recruitment-request/list";
    }

    // ================= REJECT =================
    // URL: /hr/recruitment-request/{id}/reject
    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Integer id,
            @RequestParam("reason") String reason
    ) {
        service.rejectRequest(id, reason);
        return "redirect:/hr/recruitment-request/list";
    }
}
