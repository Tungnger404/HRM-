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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository repository;
    private final RecruitmentRequestRepository reqRepo;
    private final JobDescriptionRepository jdRepo;

    @Override
    public List<JobPosting> getAll() {
        autoExpire(); // auto update status
        return repository.findAll();
    }

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
                .build();

        repository.save(posting);
    }

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

    // ===== AUTO EXPIRE =====
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
}
