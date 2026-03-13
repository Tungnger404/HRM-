package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecruitmentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reqId;

    @ManyToOne
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosition jobPosition;

    private Integer quantity;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String technicalRequirements;

    private String proposedSalary;
    private String priority;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private RecruitmentRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    private LocalDateTime createdAt;


    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    private LocalDateTime approvedAt;
    // ------------------------------------------

    @OneToOne(mappedBy = "recruitmentRequest")
    private JobDescription jobDescription;
}