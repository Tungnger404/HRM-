package com.example.hrm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class JobPostingCreateDTO {

    private Integer reqId;
    private Integer jdId;

    private String title;
    private String description;
    private String requirements;
    private String benefits;

    private LocalDate publishDate;
    private LocalDate expiryDate;
}
