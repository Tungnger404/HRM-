package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrPayrollReviewDTO {

    private Integer payslipId;
    private Integer batchId;
    private Integer empId;
    private String employeeName;
    private String jobTitle;
    private String periodLabel;

    private String batchStatus;
    private String slipStatus;

    private String rejectReason;
    private LocalDateTime rejectedAt;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate joinDate;

    private BigDecimal savedBaseSalary;
    private BigDecimal contractBaseSalary;

    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal missingWorkDays;

    private BigDecimal savedOtHours;
    private BigDecimal recalculatedOtHours;

    private BigDecimal savedIncome;
    private BigDecimal savedDeduction;
    private BigDecimal savedNet;

    private BigDecimal recalculatedIncome;
    private BigDecimal recalculatedDeduction;
    private BigDecimal recalculatedNet;

    private BigDecimal suggestedProratedBase;

    private Boolean bankMissing;
    private Boolean newHireInPeriod;
    private Boolean missingAttendance;
    private Boolean baseSalaryMismatch;
    private Boolean otMismatch;
    private Boolean deductionMismatch;
}