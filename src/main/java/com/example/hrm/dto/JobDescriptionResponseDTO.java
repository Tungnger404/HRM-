package com.example.hrm.dto;

import com.example.hrm.entity.JobDescriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDescriptionResponseDTO {

    private Integer id;
    private String jobTitle;
    private String salaryRange;
    private String workingLocation;
    private JobDescriptionStatus status;
    private LocalDateTime createdAt;
    private String responsibilities;
    private String requirements;
    private String benefits;
}
