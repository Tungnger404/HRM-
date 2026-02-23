package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "posting_id")
    private Integer postingId;

    // ===== RELATION TO RECRUITMENT REQUEST =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", nullable = false)
    private RecruitmentRequest recruitmentRequest;

    // ===== RELATION TO JOB DESCRIPTION =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id")
    private JobDescription jobDescription;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String requirements;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String benefits;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status; // OPEN / CLOSED / EXPIRED
}
