package com.example.hrm.service.impl;

import com.example.hrm.dto.*;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.JobPostingRepository;
import com.example.hrm.service.CandidateService;
import com.example.hrm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository repository;
    private final JobPostingRepository jobPostingRepository;

    public CandidateServiceImpl(CandidateRepository repository,
                                JobPostingRepository jobPostingRepository) {
        this.repository = repository;
        this.jobPostingRepository = jobPostingRepository;
    }


    @Override
    public List<CandidateListDTO> getCandidates(Integer postingId,
                                                CandidateStatus status,
                                                String keyword) {

        List<Candidate> entities =
                repository.searchCandidates(postingId, status, keyword);

        return entities.stream()
                .map(this::mapToListDTO)
                .toList();
    }

    @Override
    public CandidateEvaluateDTO getEvaluateDTO(Integer id) {

        Candidate candidate = repository.findById(id)
                .orElse(null);

        if (candidate == null) return null;

        CandidateEvaluateDTO dto = new CandidateEvaluateDTO();
        dto.setId(candidate.getCandidateId());
        dto.setPostingId(
                candidate.getJobPosting().getPostingId()
        );
        dto.setScore(candidate.getScreeningScore());

        return dto;
    }

    @Override
    public void evaluate(CandidateEvaluateDTO dto) {

        Candidate candidate = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setScreeningScore(dto.getScore());

        if ("pass".equalsIgnoreCase(dto.getAction())) {

            candidate.setStatus(CandidateStatus.INTERVIEW_SCHEDULED);

        } else {

            candidate.setStatus(CandidateStatus.REJECTED);

            // 🔥 GỬI MAIL REJECT
            emailService.sendRejectMail(
                    candidate.getEmail(),
                    candidate.getFullName(),
                    candidate.getJobPosting().getTitle()
            );
        }

        repository.save(candidate);
    }

    @Override
    public void apply(String slug, ApplyFormDTO form) {


        JobPosting job = jobPostingRepository
                .findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new RuntimeException("Job is not open");
        }


        boolean existed = repository
                .existsByEmailAndJobPosting_PostingId(
                        form.getEmail(),
                        job.getPostingId()
                );

        if (existed) {
            throw new RuntimeException("You already applied this job");
        }

        // 4️⃣ Create candidate
        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(form.getFullName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .cvUrl(form.getCvUrl())
                .status(CandidateStatus.APPLIED)   // ✅ enum
                .appliedAt(LocalDateTime.now())
                .source("WEBSITE")
                .build();

        repository.save(candidate);
    }


    @Override
    public Candidate findById(Integer id) {
        return repository.findById(id).orElse(null);
    }


    private CandidateListDTO mapToListDTO(Candidate c) {

        return new CandidateListDTO(
                c.getCandidateId(),
                c.getFullName(),
                c.getEmail(),
                c.getStatus(),
                c.getScreeningScore()
        );
    }
    @Autowired
    private EmailService emailService;
    @Override
    public void scheduleBatchInterview(BatchInterviewDTO dto) {

        for (Integer id : dto.getCandidateIds()) {

            Candidate candidate = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidate not found"));

            candidate.setStatus(CandidateStatus.INTERVIEW_SCHEDULED);
            candidate.setInterviewDate(dto.getInterviewDate());
            candidate.setInterviewLocation(dto.getLocation());

            repository.save(candidate);

            emailService.sendInterviewMail(
                    candidate.getEmail(),
                    candidate.getFullName(),
                    candidate.getJobPosting().getTitle(),
                    dto.getInterviewDate(),
                    dto.getLocation()
            );
        }
    }

}