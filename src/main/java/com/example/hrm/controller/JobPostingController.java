package com.example.hrm.controller;

import com.example.hrm.dto.JobPostingCreateDTO;
import com.example.hrm.entity.JobDescriptionStatus;
import com.example.hrm.entity.RecruitmentRequestStatus;
import com.example.hrm.repository.JobDescriptionRepository;
import com.example.hrm.repository.RecruitmentRequestRepository;
import com.example.hrm.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hr/job-posting")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService service;
    private final RecruitmentRequestRepository reqRepo;
    private final JobDescriptionRepository jdRepo;

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.getAll());
        return "job-posting/list";
    }

    // CREATE FORM
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dto", new JobPostingCreateDTO());

        model.addAttribute("requests",
                reqRepo.findByStatus(RecruitmentRequestStatus.APPROVED));

        model.addAttribute("descriptions",
                jdRepo.findByStatus(JobDescriptionStatus.ACTIVE));

        return "job-posting/create";
    }


    // CREATE
    @PostMapping("/create")
    public String create(@ModelAttribute JobPostingCreateDTO dto) {
        service.create(dto);
        return "redirect:/hr/job-posting?success";
    }

    // CHANGE STATUS
    @GetMapping("/status/{id}/{status}")
    public String changeStatus(@PathVariable Integer id,
                               @PathVariable String status) {
        service.changeStatus(id, status);
        return "redirect:/hr/job-posting";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/hr/job-posting";
    }
}
