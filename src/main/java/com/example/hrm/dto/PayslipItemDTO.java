package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipItemDTO {
    private Long id;
    private String code;
    private String name;
    private BigDecimal amount;
    private String type;       // INCOME/DEDUCTION/INFO
    private Boolean manual;
}
