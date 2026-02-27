package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRowDTO {

    private int payslipId;
    private int batchId;

    private int empId;
    private String empCode;
    private String fullName;

    private int month;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;

    private String jobTitle;
    private String batchName;

    // Work metrics
    private BigDecimal baseSalary;        // lương cơ bản/vị trí
    private BigDecimal standardWorkDays;  // ngày công chuẩn
    private BigDecimal actualWorkDays;    // ngày công thực tế
    private BigDecimal otHours;           // OT giờ
    private BigDecimal dailySalary;       // lương ngày (base/standard)

    // Totals
    private BigDecimal totalIncome;       // gross
    private BigDecimal totalDeduction;    // deduction
    private BigDecimal netSalary;         // net

    private String batchStatus;
    private String statusLabel;

    // Enterprise flags
    private Boolean sentToEmployee;  // đã release cho employee chưa
    private Boolean bankMissing;     // thiếu bank account primary hay không
}