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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", nullable = false)
    private RecruitmentRequest recruitmentRequest;

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

    @Column(length = 150)
    private String location;

    @Column(name = "employment_type", length = 100)
    private String employmentType;

    @Builder.Default
    private Boolean isHot = false;

    private LocalDate publishDate;
    private LocalDate expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN";

    @Column(unique = true, length = 200)
    private String slug;

    @Builder.Default
    private Boolean isPublic = true;

    @Builder.Default
    private Integer viewCount = 0;

    @Transient
    private Long candidateCount;
}