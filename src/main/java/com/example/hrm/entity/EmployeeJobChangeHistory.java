package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_job_change_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeJobChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Integer changeId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "from_job_id")
    private Integer fromJobId;

    @Column(name = "to_job_id")
    private Integer toJobId;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_job_id", insertable = false, updatable = false)
    private JobPosition fromJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_job_id", insertable = false, updatable = false)
    private JobPosition toJob;
}