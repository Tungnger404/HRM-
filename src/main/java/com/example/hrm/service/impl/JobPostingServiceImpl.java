package com.example.hrm.service.impl;

import com.example.hrm.dto.JobPostingCreateDTO;
import com.example.hrm.entity.*;
import com.example.hrm.repository.JobDescriptionRepository;
import com.example.hrm.repository.JobPostingRepository;
import com.example.hrm.repository.RecruitmentRequestRepository;
import com.example.hrm.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository repository;
    private final RecruitmentRequestRepository reqRepo;
    private final JobDescriptionRepository jdRepo;

    // ================================
    // 1️⃣ HR GET ALL
    // ================================
    @Override
    public List<JobPosting> getAll() {
        autoExpire();
        return repository.findAll();
    }

    // ================================
    // 2️⃣ CREATE
    // ================================
    @Override
    public void create(JobPostingCreateDTO dto) {

        RecruitmentRequest req =
                reqRepo.findById(dto.getReqId())
                        .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RecruitmentRequestStatus.APPROVED) {
            throw new RuntimeException("Recruitment Request must be APPROVED");
        }

        JobDescription jd =
                jdRepo.findById(dto.getJdId())
                        .orElseThrow(() -> new RuntimeException("Job Description not found"));

        if (jd.getStatus() != JobDescriptionStatus.ACTIVE) {
            throw new RuntimeException("Job Description must be ACTIVE");
        }

        if (dto.getExpiryDate().isBefore(dto.getPublishDate())) {
            throw new RuntimeException("Expiry date must be after publish date");
        }

        String slug = generateUniqueSlug(dto.getTitle());

        JobPosting posting = JobPosting.builder()
                .recruitmentRequest(req)
                .jobDescription(jd)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .benefits(dto.getBenefits())
                .publishDate(dto.getPublishDate())
                .expiryDate(dto.getExpiryDate())
                .status("OPEN")
                .slug(slug)
                .isPublic(true)
                .viewCount(0)
                .build();

        repository.save(posting);
    }

    // ================================
    // 3️⃣ CHANGE STATUS
    // ================================
    @Override
    public void changeStatus(Integer id, String status) {

        JobPosting posting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Posting not found"));

        posting.setStatus(status);
        repository.save(posting);
    }

    // ================================
    // 4️⃣ DELETE
    // ================================
    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    // ================================
    // 5️⃣ AUTO EXPIRE
    // ================================
    @Override
    public void autoExpire() {

        List<JobPosting> openList = repository.findByStatus("OPEN");

        for (JobPosting jp : openList) {
            if (jp.getExpiryDate() != null &&
                    jp.getExpiryDate().isBefore(LocalDate.now())) {

                jp.setStatus("EXPIRED");
            }
        }
    }

    // ================================
    // 6️⃣ PUBLIC CAREER PAGE
    // ================================
    @Override
    public List<JobPosting> getPublicOpenJobs() {
        return repository.findByIsPublicTrueAndStatusAndExpiryDateAfter(
                "OPEN",
                LocalDate.now()
        );
    }

    @Override
    public JobPosting getBySlug(String slug) {
        return repository.findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    @Override
    public void increaseViewCount(String slug) {

        JobPosting job = repository.findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setViewCount(job.getViewCount() + 1);
    }

    // ================================
    // 7️⃣ SLUG GENERATOR
    // ================================
    private String generateUniqueSlug(String title) {

        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int counter = 1;

        while (repository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String toSlug(String input) {

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}