package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payslips",
        uniqueConstraints = @UniqueConstraint(name = "uq_slip_batch_emp", columnNames = {"batch_id", "emp_id"}))
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

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    @Column(name = "standard_work_days")
    private BigDecimal standardWorkDays;

    @Column(name = "actual_work_days")
    private BigDecimal actualWorkDays;

    @Column(name = "ot_hours")
    private BigDecimal otHours;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @Column(name = "total_deduction")
    private BigDecimal totalDeduction;

    @Column(name = "net_salary")
    private BigDecimal netSalary;

    @Column(name = "is_sent_to_employee")
    private Boolean sentToEmployee;
}
