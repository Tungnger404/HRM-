package com.example.hrm.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RecruitmentRequestCreateDTO {
    private Integer departmentId;
    private Integer jobId;
    private Integer quantity;
    private String reason;
    private String technicalRequirements; // Mới
    private String proposedSalary;       // Mới
    private String priority;             // Mới

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    private Integer creatorId;
}
