package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payroll_batches")
public class PayrollBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PayrollPeriod period;

    @Column(name = "name")
    private String name;

    @Column(name = "total_gross")
    private BigDecimal totalGross;

    @Column(name = "total_net")
    private BigDecimal totalNet;

    @Column(name = "status")
    private String status; // DRAFT/PENDING_APPROVAL/APPROVED/PAID

    @Column(name = "created_by")
    private Integer createdBy;   // emp_id

    @Column(name = "approved_by")
    private Integer approvedBy;  // emp_id
}
