package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "request_type", length = 20)
    private String requestType; // LEAVE / OVERTIME

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(length = 20)
    private String status; // PENDING / APPROVED / REJECTED

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String approverNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }
}
