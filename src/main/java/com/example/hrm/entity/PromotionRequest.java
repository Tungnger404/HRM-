package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "current_position_id")
    private Integer currentPositionId;

    @Column(name = "proposed_position_id")
    private Integer proposedPositionId;

    @Column(name = "requested_by", nullable = false)
    private Integer requestedBy;

    @Column(name = "request_reason", columnDefinition = "NVARCHAR(MAX)")
    private String requestReason;

    @Column(name = "evaluation_summary", columnDefinition = "NVARCHAR(MAX)")
    private String evaluationSummary;

    @Column(name = "avg_score")
    private Double avgScore;

    @Column(name = "evaluation_count")
    private Integer evaluationCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RequestStatus status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "hr_comment", columnDefinition = "NVARCHAR(MAX)")
    private String hrComment;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
