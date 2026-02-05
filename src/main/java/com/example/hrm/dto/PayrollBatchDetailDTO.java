package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatchDetailDTO {
    private Integer batchId;
    private Integer periodId;
    private String batchName;
    private String status;

    private BigDecimal totalGross;
    private BigDecimal totalNet;

    private List<PayslipSummaryDTO> payslips;
}
