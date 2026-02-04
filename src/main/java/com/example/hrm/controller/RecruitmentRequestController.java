package com.example.hrm.controller;

import com.example.hrm.dto.RecruitmentRequestCreateDTO;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.service.RecruitmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recruitment-request")
@RequiredArgsConstructor
public class RecruitmentRequestController {

    private final RecruitmentRequestService recruitmentRequestService;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;

    // 👉 Mở form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("dto", new RecruitmentRequestCreateDTO());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobPositionRepository.findAll());
        return "recruitment-request-create";
    }

    // 👉 Submit form
    @PostMapping("/create")
    public String create(@ModelAttribute("dto") RecruitmentRequestCreateDTO dto) {
        recruitmentRequestService.createRecruitmentRequest(dto);
        return "redirect:/recruitment-request/create?success=true";

    }

}
