package com.example.hrm.dto;

import lombok.Data;

@Data
public class RecruitmentRequestResponseDTO {

    private Integer reqId;
    private String jobTitle;
    private Integer quantity;
    private String departmentName;
}