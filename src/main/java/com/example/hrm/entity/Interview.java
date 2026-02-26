package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Integer interviewId;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "interviewer_id")
    private Employee interviewer;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    private String location;

    @Column(name = "round_number")
    private Integer roundNumber; // 1 = HR, 2 = Manager

    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    private InterviewResult result;
}