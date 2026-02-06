package com.example.hrm.controller;

import com.example.hrm.service.RecruitmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr/recruitment-request")
@RequiredArgsConstructor
public class RecruitmentRequestHRController {

    private final RecruitmentRequestService recruitmentRequestService;

    @GetMapping("/list")
    public String viewRecruitmentRequests(Model model) {
        model.addAttribute(
                "requests",
                recruitmentRequestService.getRequestsForHR()
        );
        return "recruitment-request-list";
    }
}
