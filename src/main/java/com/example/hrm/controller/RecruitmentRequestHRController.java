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

    // LIST + SEARCH + FILTER
    @GetMapping("/list")
    public String viewRecruitmentRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecruitmentRequestStatus status,
            Model model) {

        List<RecruitmentRequest> requests =
                recruitmentRequestService.searchRequests(keyword, status);

        model.addAttribute("requests", requests);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "recruitment-request/hr-list";
    }


    // VIEW DETAIL
    @GetMapping("/view/{id}")
    public String viewDetail(
            @PathVariable Integer id,
            Model model) {

        RecruitmentRequest request =
                recruitmentRequestService.getById(id);

        model.addAttribute("request", request);

        return "recruitment-request/detail";
    }

}