package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private Integer candidateId;

    // ===== RELATION TO JOB POSTING =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "cv_url")
    private String cvUrl;

    @Column(nullable = false)
    private String status; // APPLIED / SCREENING / INTERVIEW / OFFER / REJECTED

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "screening_score")
    private Integer screeningScore;

    // ===== NEW FIELD FROM YOUR DB UPDATE =====
    @Column(nullable = false)
    @Builder.Default
    private String source = "WEBSITE";
}