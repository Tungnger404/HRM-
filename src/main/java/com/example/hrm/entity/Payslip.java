package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "payslips",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_slip_batch_emp",
                columnNames = {"batch_id", "emp_id"}
        )
)
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payslip_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private PayrollBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(name = "base_salary", precision = 20, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "standard_work_days", precision = 20, scale = 2)
    private BigDecimal standardWorkDays;

    @Column(name = "actual_work_days", precision = 20, scale = 2)
    private BigDecimal actualWorkDays;

    @Column(name = "ot_hours", precision = 20, scale = 2)
    private BigDecimal otHours;

    @Column(name = "total_income", precision = 20, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_deduction", precision = 20, scale = 2)
    private BigDecimal totalDeduction;

    @Column(name = "net_salary", precision = 20, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "is_sent_to_employee")
    private Boolean sentToEmployee;

    @Column(name = "slip_status", nullable = false, length = 20)
    @Builder.Default
    private String slipStatus = "ACTIVE";

    @Column(name = "reject_reason", length = 1000)
    private String rejectReason;

    @Column(name = "rejected_by")
    private Integer rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
}
