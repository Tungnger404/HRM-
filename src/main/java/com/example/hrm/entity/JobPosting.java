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

    // ================= NEW FILTER FIELDS =================

    @Column(length = 150)
    private String location; // dùng cho dropdown filter

    @Column(name = "employment_type", length = 100)
    private String employmentType; // Full-time / Intern / Remote

    @Column(
            name = "is_hot",
            nullable = false,
            columnDefinition = "bit default 0"
    )
    @Builder.Default
    private Boolean isHot = false;

    // ================= DATE =================

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // ================= STATUS =================

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN"; // OPEN / CLOSED / EXPIRED

    // ================= PUBLIC PORTAL =================

    @Column(unique = true, length = 200)
    private String slug; // dùng cho SEO URL: /careers/java-developer

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true; // chỉ hiển thị job public

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    // ================= STATISTIC =================

    @Transient
    private Long candidateCount; // dùng hiển thị count ngoài UI
}