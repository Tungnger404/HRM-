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

    // ✅ NEW: hiển thị kỳ lương để biết lương mới/cũ
    // ví dụ: "02/2026 (2026-02-01 → 2026-02-29)"
    private String period;

    private BigDecimal totalIncome;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;
}
