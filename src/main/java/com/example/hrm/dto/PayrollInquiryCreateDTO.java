package com.example.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollInquiryCreateDTO {

    @NotNull
    private Integer payslipId;

    @NotBlank
    private String question;
}
