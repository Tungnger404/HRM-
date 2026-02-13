package com.example.hrm.dto;

import lombok.Data;

@Data
public class JobDescriptionCreateDTO {

    private Integer jobId;
    private String responsibilities;
    private String requirements;
    private String benefits;
    private String salaryRange;
    private String workingLocation;
}
