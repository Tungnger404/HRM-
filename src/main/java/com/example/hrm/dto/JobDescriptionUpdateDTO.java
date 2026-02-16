package com.example.hrm.dto;

import com.example.hrm.entity.JobDescriptionStatus;
import lombok.Data;

@Data
public class JobDescriptionUpdateDTO {

    private String responsibilities;
    private String requirements;
    private String benefits;
    private String salaryRange;
    private String workingLocation;
    private JobDescriptionStatus status;
}
