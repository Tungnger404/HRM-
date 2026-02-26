package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "job_level")
    private Integer jobLevel;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}