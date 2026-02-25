package com.example.hrm.service.impl;

import com.example.hrm.dto.*;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.JobPostingRepository;
import com.example.hrm.service.CandidateService;
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

    // ================================
    // 1️⃣ GET LIST WITH FILTER
    // ================================
    @Override
    public List<CandidateListDTO> getCandidates(Integer postingId,
                                                String status,
                                                String keyword) {

        List<Candidate> entities =
                repository.searchCandidates(postingId, status, keyword);

        return entities.stream()
                .map(this::mapToListDTO)
                .toList();
    }

    // ================================
    // 2️⃣ GET EVALUATE DTO
    // ================================
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

    // ================================
    // 3️⃣ EVALUATE
    // ================================
    @Override
    public void evaluate(CandidateEvaluateDTO dto) {

        Candidate candidate = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setScreeningScore(dto.getScore());

        if ("pass".equalsIgnoreCase(dto.getAction())) {
            candidate.setStatus("INTERVIEW");
        } else {
            candidate.setStatus("REJECTED");
        }

        repository.save(candidate);
    }

    // ================================
    // 4️⃣ APPLY FROM CAREER PAGE
    // ================================
    @Override
    public void apply(String slug, ApplyFormDTO form) {

        // 1️⃣ Find job by slug
        JobPosting job = jobPostingRepository
                .findBySlugAndIsPublicTrue(slug)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 2️⃣ Check job is OPEN
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new RuntimeException("Job is not open");
        }

        // 3️⃣ Check duplicate email
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
                .status("APPLIED")
                .appliedAt(LocalDateTime.now())
                .source("WEBSITE")
                .build();

        repository.save(candidate);
    }

    // ================================
    // 5️⃣ FIND BY ID
    // ================================
    @Override
    public Candidate findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // ================================
    // 6️⃣ MAPPER
    // ================================
    private CandidateListDTO mapToListDTO(Candidate c) {

        return new CandidateListDTO(
                c.getCandidateId(),
                c.getFullName(),
                c.getEmail(),
                c.getStatus(),
                c.getScreeningScore()
        );
    }
}