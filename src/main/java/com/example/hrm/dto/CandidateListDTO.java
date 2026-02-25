package com.example.hrm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateListDTO {

    private Integer candidateId;
    private String fullName;
    private String email;
    private String status;
    private Integer screeningScore;
}