package com.example.hrm.controller;

import com.example.hrm.dto.ApplyFormDTO;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.service.CandidateService;
import com.example.hrm.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/careers")
public class CareerController {

    private final JobPostingService jobPostingService;
    private final CandidateService candidateService;

    // =====================================
    // 1️⃣ LIST PUBLIC JOBS
    // =====================================
    @GetMapping
    public String careerPage(Model model) {

        model.addAttribute("jobs",
                jobPostingService.getPublicOpenJobs());

        return "career/list";   // templates/career/list.html
    }

    // =====================================
    // 2️⃣ JOB DETAIL
    // =====================================
    @GetMapping("/{slug}")
    public String jobDetail(@PathVariable String slug,
                            Model model) {

        JobPosting job = jobPostingService.getBySlug(slug);

        jobPostingService.increaseViewCount(slug);

        model.addAttribute("job", job);
        model.addAttribute("applyForm", new ApplyFormDTO());

        return "career/detail";  // templates/career/detail.html
    }

    // =====================================
    // 3️⃣ APPLY
    // =====================================
    @PostMapping("/{slug}/apply")
    public String apply(@PathVariable String slug,
                        @ModelAttribute("applyForm") ApplyFormDTO form,
                        Model model) {

        try {
            candidateService.apply(slug, form);
            return "redirect:/careers/" + slug + "/success";
        } catch (RuntimeException e) {

            // load lại job để hiển thị trang detail
            JobPosting job = jobPostingService.getBySlug(slug);

            model.addAttribute("job", job);
            model.addAttribute("applyForm", form); // giữ lại dữ liệu người dùng nhập
            model.addAttribute("error", e.getMessage());

            return "career/detail";
        }
    }

    // =====================================
    // 4️⃣ APPLY SUCCESS PAGE
    // =====================================
    @GetMapping("/{slug}/success")
    public String successPage(@PathVariable String slug,
                              Model model) {

        model.addAttribute("job",
                jobPostingService.getBySlug(slug));

        return "career/success";  // templates/career/success.html
    }
}