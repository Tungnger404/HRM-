package com.example.hrm.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollInquiryDTO {
    private Integer id;
    private Integer payslipId;
    private Integer empId;
    private String question;
    private String answer;
    private String status;
    private LocalDateTime createdAt;
}
