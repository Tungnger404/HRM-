package com.example.hrm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignmentUpdateRequest {
    private Integer deptId;
    private Integer jobId;
    private String reason;
}