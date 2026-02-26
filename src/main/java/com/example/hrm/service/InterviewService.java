package com.example.hrm.service;

import com.example.hrm.entity.InterviewResult;

import java.time.LocalDateTime;

public interface InterviewService {

    void submitEvaluation(Integer candidateId,
                          Integer roundNumber,
                          Integer score,
                          String feedback,
                          InterviewResult result);
    void scheduleInterview(Integer candidateId,
                           Integer roundNumber,
                           LocalDateTime time,
                           String location);
}