package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_descriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jd_id")
    private Integer id;

    // --- KẾT NỐI VỚI REQUEST GỐC ---
    @OneToOne
    @JoinColumn(name = "req_id", unique = true)
    private RecruitmentRequest recruitmentRequest;
    // ----------------------------

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosition job;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String responsibilities;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String requirements;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String benefits;

    private String salaryRange;
    private String workingLocation;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Employee createdBy;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private JobDescriptionStatus status;
}