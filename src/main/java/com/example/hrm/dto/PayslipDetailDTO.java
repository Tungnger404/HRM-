package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipDetailDTO {
    private Integer payslipId;
    private Integer batchId;

    private Integer empId;
    private String employeeName;

    private String period;

    private BigDecimal baseSalary;
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal otHours;

    private BigDecimal totalIncome;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;

    private List<PayslipItemDTO> items;
}
