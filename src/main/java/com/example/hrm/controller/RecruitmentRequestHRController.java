package com.example.hrm.controller;

import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.entity.RecruitmentRequestStatus;
import com.example.hrm.service.RecruitmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/hr/recruitment-request")
@RequiredArgsConstructor
public class RecruitmentRequestHRController {

    private final RecruitmentRequestService recruitmentRequestService;

    @GetMapping("/list")
    public String viewRecruitmentRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecruitmentRequestStatus status,
            @RequestParam(required = false) String priority,
            Model model) {

        List<RecruitmentRequest> requests = recruitmentRequestService.searchRequests(keyword, status, priority);

        model.addAttribute("requests", requests);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        return "recruitment-request/hr-list";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        RecruitmentRequest request = recruitmentRequestService.getById(id);
        model.addAttribute("request", request);
        return "recruitment-request/detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id) {
        recruitmentRequestService.approveRequest(id);
        return "redirect:/hr/recruitment-request/list?approved=true";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id, @RequestParam("reason") String reason) {
        recruitmentRequestService.rejectRequest(id, reason);
        return "redirect:/hr/recruitment-request/list?rejected=true";
    }
}