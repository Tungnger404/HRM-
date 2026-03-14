package com.example.hrm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftAssignmentFormDTO {
    private Long batchId;
    private Integer empId;
    private String workDate;       // yyyy-MM-dd
    private String assignmentType; // WORK / OFF / LEAVE / HOLIDAY
    private Integer shiftId;       // required when WORK
    private String note;
}