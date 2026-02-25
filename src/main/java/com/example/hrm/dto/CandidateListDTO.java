package com.example.hrm.dto;

import com.example.hrm.entity.CandidateStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateListDTO {

    private Integer candidateId;
    private String fullName;
    private String email;
    private CandidateStatus status;
    private Integer screeningScore;
}