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

    @Override
    public List<JobPosting> getAll() {
        autoExpire(); // Tự động cập nhật các tin hết hạn trước khi lấy danh sách
        return repository.findAll();
    }

    @Override
    public void create(JobPostingCreateDTO dto) {
        RecruitmentRequest req = reqRepo.findById(dto.getReqId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        JobDescription jd = jdRepo.findById(dto.getJdId())
                .orElseThrow(() -> new RuntimeException("Job Description not found"));

        // Tạo Slug duy nhất dựa trên tiêu đề bài đăng
        String slug = generateUniqueSlug(dto.getTitle());

        // ✅ Logic: Nếu HR để trống ô nhập, hệ thống tự bốc dữ liệu từ JD sang
        String finalDescription = (dto.getDescription() == null || dto.getDescription().isBlank())
                ? jd.getResponsibilities() : dto.getDescription();

        String finalLocation = (dto.getLocation() == null || dto.getLocation().isBlank())
                ? jd.getWorkingLocation() : dto.getLocation();

        JobPosting posting = JobPosting.builder()
                .recruitmentRequest(req)
                .jobDescription(jd)
                .title(dto.getTitle())
                .description(finalDescription)
                .requirements(dto.getRequirements() == null || dto.getRequirements().isBlank() ? jd.getRequirements() : dto.getRequirements())
                .benefits(dto.getBenefits() == null || dto.getBenefits().isBlank() ? jd.getBenefits() : dto.getBenefits())
                .location(finalLocation)
                .publishDate(dto.getPublishDate())
                .expiryDate(dto.getExpiryDate())
                .status("OPEN")
                .slug(slug)
                .isPublic(true)
                .viewCount(0)
                .build();

        repository.save(posting);
    }
    // Các hàm changeStatus, delete, autoExpire giữ nguyên logic của bạn...

    @Override
    public void changeStatus(Integer id, String status) {
        JobPosting posting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Posting not found"));
        posting.setStatus(status);
        repository.save(posting);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public void autoExpire() {
        List<JobPosting> openList = repository.findByStatus("OPEN");
        LocalDate today = LocalDate.now();
        for (JobPosting jp : openList) {
            if (jp.getExpiryDate() != null && jp.getExpiryDate().isBefore(today)) {
                jp.setStatus("EXPIRED");
            }
        }
    }

    // Luồng Public Career Page
    @Override
    public List<JobPosting> getPublicOpenJobs() {
        return repository.findByIsPublicTrueAndStatusAndExpiryDateAfter("OPEN", LocalDate.now());
    }

    @Override
    public JobPosting getBySlug(String slug) {
        return repository.findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    @Override
    public void increaseViewCount(String slug) {
        repository.findBySlugAndIsPublicTrue(slug).ifPresent(job -> {
            job.setViewCount(job.getViewCount() + 1);
            repository.save(job);
        });
    }

    // Slug Generator
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
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}