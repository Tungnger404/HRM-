package com.example.hrm.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPositionForm {

    private Integer jobId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title max 200 characters")
    private String title;

    @NotNull(message = "Job level is required")
    @Min(value = 1, message = "Job level must be >= 1")
    @Max(value = 10, message = "Job level must be <= 10")
    private Integer jobLevel;

    @Size(max = 2000, message = "Description max 2000 characters")
    private String description;

    // filter/list use
    private Boolean active;
}