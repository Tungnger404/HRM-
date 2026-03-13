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
            @RequestParam(required = false) String priority, // Thêm dòng này
            Model model) {

        // Cập nhật Service để lọc thêm theo priority
        List<RecruitmentRequest> requests = recruitmentRequestService.searchRequests(keyword, status, priority);

        model.addAttribute("requests", requests);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority); // Gửi ngược lại để giữ trạng thái dropdown
        return "recruitment-request/hr-list";
    }

    // Khớp với th:href="@{/hr/recruitment-request/detail/{id}(id=${req.reqId})}"
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        RecruitmentRequest request = recruitmentRequestService.getById(id);
        model.addAttribute("request", request);
        return "recruitment-request/detail";
    }

    // Xử lý Approve từ form trong detail.html
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id) {
        recruitmentRequestService.approveRequest(id);
        return "redirect:/hr/recruitment-request/list?approved=true";
    }

    // Xử lý Reject từ form trong detail.html
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id, @RequestParam("reason") String reason) {
        recruitmentRequestService.rejectRequest(id, reason);
        return "redirect:/hr/recruitment-request/list?rejected=true";
    }
}