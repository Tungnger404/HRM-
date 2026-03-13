package com.example.hrm.dto;

import lombok.Data;

@Data
public class JobDescriptionCreateDTO {
    private Integer requestId;
    private Integer jobId;
    private String jobTitle;
    private String description;
    private String responsibilities;
    private String requirements;
    private String benefits;
    private String salaryRange;
    private String workingLocation;
}