package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatchSummaryDTO {
    private Integer id;
    private Integer periodId;
    private String name;
    private String status;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
}
