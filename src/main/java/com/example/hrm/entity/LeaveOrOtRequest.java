package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "requests")
public class LeaveOrOtRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 20, nullable = false)
    private RequestType requestType;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String approverNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "target_work_date")
    private LocalDate targetWorkDate;

    @Column(name = "manager_decided_at")
    private LocalDateTime managerDecidedAt;

    @Column(name = "processed_by_hr")
    private Integer processedByHr;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "related_assignment_id")
    private Long relatedAssignmentId;

    @Column(name = "requested_shift_id")
    private Integer requestedShiftId;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }
}