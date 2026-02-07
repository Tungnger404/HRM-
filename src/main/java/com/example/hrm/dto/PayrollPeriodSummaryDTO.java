package com.example.hrm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodSummaryDTO {
    private Integer id;
    private String name;
    private Integer month;
    private Integer year;
    private String status;
    private Boolean locked;
}
