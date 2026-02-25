package com.example.hrm.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BatchInterviewDTO {

    private List<Integer> candidateIds;
    private LocalDateTime interviewDate;
    private String location;
    private String interviewer;

    public List<Integer> getCandidateIds() {
        return candidateIds;
    }

    public void setCandidateIds(List<Integer> candidateIds) {
        this.candidateIds = candidateIds;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }
}