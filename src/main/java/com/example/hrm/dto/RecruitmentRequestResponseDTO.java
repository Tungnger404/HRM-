package com.example.hrm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RecruitmentRequestResponseDTO {
    private Integer reqId;
    private String jobTitle;
    private String departmentName;
    private Integer quantity;
    private String status;               // SUBMITTED, APPROVED, etc.
    private String priority;
    private LocalDate deadline;
}