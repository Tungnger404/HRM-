package com.example.hrm.controller;

import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.JobPostingRepository;

import com.example.hrm.service.CloudinaryService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/careers")
public class PublicRecruitmentController {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String careerHome(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer department,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (location != null && location.isBlank()) location = null;

        Pageable pageable = PageRequest.of(page, 6);

        Page<JobPosting> jobPage =
                jobPostingRepository.searchPublicJobs(
                        keyword,
                        location,
                        department,
                        LocalDate.now(),
                        pageable
                );

        if (page >= jobPage.getTotalPages() && jobPage.getTotalPages() > 0) {
            return "redirect:/careers?page=0";
        }

        model.addAttribute("jobPage", jobPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("department", department);
        model.addAttribute("locations",
                jobPostingRepository.findDistinctPublicLocations(LocalDate.now()));
        model.addAttribute("departments",
                departmentRepository.findAll());
        model.addAttribute("activePage", "careers");

        return "public/home";
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Page<JobPosting>> searchAjax(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer department,
            @RequestParam(defaultValue = "0") int page
    ) {

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (location != null && location.isBlank()) location = null;

        Pageable pageable = PageRequest.of(page, 6);

        Page<JobPosting> jobPage =
                jobPostingRepository.searchPublicJobs(
                        keyword,
                        location,
                        department,
                        LocalDate.now(),
                        pageable
                );

        return ResponseEntity.ok(jobPage);
    }

    @GetMapping("/{slug}")
    public String jobDetail(
            @PathVariable String slug,
            Model model
    ) {

        JobPosting job = jobPostingRepository
                .findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // increase view
        job.setViewCount(job.getViewCount() + 1);
        jobPostingRepository.save(job);

        model.addAttribute("job", job);
        model.addAttribute("activePage", "careers");

        return "public/job-detail";
    }

    @GetMapping("/{slug}/apply")
    public String applyForm(
            @PathVariable String slug,
            Model model
    ) {

        JobPosting job = jobPostingRepository
                .findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        model.addAttribute("job", job);

        return "public/apply";
    }


    @PostMapping("/apply")
    public String applySubmit(
            @RequestParam Integer postingId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String gender,   // THÊM MỚI
            @RequestParam String dob,      // THÊM MỚI (Dạng String từ input date)
            @RequestParam String address,  // THÊM MỚI
            @RequestParam MultipartFile cvFile,
            Model model
    ) throws IOException {

        JobPosting job = jobPostingRepository
                .findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (candidateRepository.existsByEmailAndJobPosting_PostingId(email, postingId)) {
            model.addAttribute("error", "You already applied this job");
            model.addAttribute("job", job);
            return "public/apply";
        }

        String secureUrl = cloudinaryService.uploadFile(cvFile);

        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .gender(gender)
                .dob(LocalDate.parse(dob))
                .address(address)
                .cvUrl(secureUrl)
                .status(CandidateStatus.APPLIED)
                .build();

        candidateRepository.save(candidate);

        return "redirect:/careers/" + job.getSlug() + "?applied=true";
    }
}
