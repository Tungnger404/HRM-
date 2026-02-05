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
    private BigDecimal netSalary;
    private String statusLabel;
    private String batchStatus;
}
