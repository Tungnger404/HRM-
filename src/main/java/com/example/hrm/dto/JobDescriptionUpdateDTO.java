package com.example.hrm.dto;

import lombok.Data;

@Data
public class JobDescriptionUpdateDTO {

    private String responsibilities;
    private String requirements;
    private String benefits;
    private String salaryRange;
    private String workingLocation;
    private String status;
}
