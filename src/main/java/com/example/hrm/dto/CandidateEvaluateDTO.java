package com.example.hrm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEvaluateDTO {

    private Integer id;
    private Integer postingId;
    private Integer score;
    private String fullName;
    private String action;
}