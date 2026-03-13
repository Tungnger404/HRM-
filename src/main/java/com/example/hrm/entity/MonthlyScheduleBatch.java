package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_schedule_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyScheduleBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "schedule_month", nullable = false)
    private LocalDate scheduleMonth; // always first day of month

    @Column(name = "status", nullable = false, length = 20)
    private String status; // DRAFT / PUBLISHED / CLOSED

    @Column(name = "created_by", nullable = false)
    private Integer createdBy; // emp_id

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "note", length = 500)
    private String note;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) status = "DRAFT";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
