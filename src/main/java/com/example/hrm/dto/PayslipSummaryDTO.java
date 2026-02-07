package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipSummaryDTO {
    private Integer payslipId;
    private Integer batchId;

    private Integer empId;
    private String employeeName;

    private BigDecimal totalIncome;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;
}
