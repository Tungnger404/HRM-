package com.example.hrm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyScheduleBatchFormDTO {
    private String scheduleMonth; // format: yyyy-MM
    private String note;
}
