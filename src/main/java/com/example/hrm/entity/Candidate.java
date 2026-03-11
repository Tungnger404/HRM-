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
@Table(
        name = "candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_email_job",
                        columnNames = {"email", "posting_id"}
                )
        },
        indexes = {
                @Index(name = "idx_candidate_email", columnList = "email"),
                @Index(name = "idx_candidate_status", columnList = "status")
        }
)
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private Integer candidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "cv_url", nullable = false, length = 255)
    private String cvUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "screening_score")
    private Integer screeningScore;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "interview_location", length = 200)
    private String interviewLocation;

    @Column(nullable = false)
    @Builder.Default
    private String source = "WEBSITE";

    // ================= AUTO SET APPLY TIME =================
    @PrePersist
    public void prePersist() {
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
    }
}