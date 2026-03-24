package com.example.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateEvaluateDTO {

    private Integer id;
    private Integer postingId;

    @NotNull(message = "Vui lòng nhập điểm số")
    @Min(value = 0, message = "Điểm thấp nhất là 0")
    @Max(value = 100, message = "Điểm cao nhất là 100")
    private Integer score;

    private String fullName;

    @NotNull(message = "Hành động không được để trống")
    private String action;
}