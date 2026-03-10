package com.example.hrm.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class BenefitUpsertDTO {
    private Integer id; // dùng cho update

    private String code;
    private String name;         // tên phúc lợi
    private String type;         // INCOME / DEDUCTION
    private String calcMethod;   // FIXED / PERCENT_BASE
    private BigDecimal value;    // FIXED=amount, PERCENT_BASE=0.03

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    private Boolean active;
}