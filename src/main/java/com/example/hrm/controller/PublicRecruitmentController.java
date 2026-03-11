package com.example.hrm.controller;

import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.JobPostingRepository;

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

    // =====================================================
    // ================= CAREER HOME =======================
    // URL: /careers
    // =====================================================

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

    // =====================================================
    // ================= AJAX SEARCH =======================
    // URL: /careers/search
    // =====================================================

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

    // =====================================================
    // ================= JOB DETAIL ========================
    // URL: /careers/{slug}
    // =====================================================

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

    // =====================================================
    // ================= APPLY FORM ========================
    // URL: /careers/{slug}/apply
    // =====================================================

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

    // =====================================================
    // ================= APPLY SUBMIT ======================
    // POST: /careers/apply
    // =====================================================

    @PostMapping("/apply")
    public String applySubmit(
            @RequestParam Integer postingId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam MultipartFile cvFile,
            Model model
    ) throws IOException {

        JobPosting job = jobPostingRepository
                .findById(postingId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Prevent duplicate apply
        boolean exists = candidateRepository
                .existsByEmailAndJobPosting_PostingId(email, postingId);

        if (exists) {

            model.addAttribute("error", "You already applied this job");
            model.addAttribute("job", job);

            return "public/apply";
        }

        // Upload CV
        String uploadDir = "uploads/";

        Files.createDirectories(Paths.get(uploadDir));

        String fileName = UUID.randomUUID() + "_"
                + Objects.requireNonNull(cvFile.getOriginalFilename());

        Path filePath = Paths.get(uploadDir + fileName);

        Files.write(filePath, cvFile.getBytes());

        // Save candidate
        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .cvUrl(fileName)
                .status(CandidateStatus.APPLIED)
                .build();

        candidateRepository.save(candidate);

        return "redirect:/careers/" + job.getSlug() + "?applied=true";
    }
}