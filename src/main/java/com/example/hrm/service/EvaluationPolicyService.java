package com.example.hrm.service;

import org.springframework.stereotype.Service;

@Service
public class EvaluationPolicyService {

    public String calculateClassification(Integer score) {
        if (score == null) {
            return "D";
        }
        if (score >= 85) {
            return "A";
        } else if (score >= 75) {
            return "B";
        } else if (score >= 60) {
            return "C";
        } else {
            return "D";
        }
    }
}
