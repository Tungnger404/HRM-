package com.example.hrm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyFormDTO {

    private String fullName;
    private String email;
    private String phone;
    private String cvUrl;
}