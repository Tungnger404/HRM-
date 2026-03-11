package com.example.hrm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSearchResultDTO {
    private Integer empId;
    private String empCode;
    private String fullName;
    private String displayText;
}