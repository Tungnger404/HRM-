package com.example.hrm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobDescriptionResponseDTO {

    private Integer id;
    private String jobTitle;
    private String salaryRange;
    private String workingLocation;
    private String status;
    private LocalDateTime createdAt;
}
