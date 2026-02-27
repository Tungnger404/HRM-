package com.example.hrm.entity;

public enum CandidateStatus {

    APPLIED,
    SCREENING,
    INTERVIEW_SCHEDULED,
    INTERVIEWED,
    EVALUATED,

    OFFERED,          // đã tạo offer
    OFFER_ACCEPTED,   // ứng viên accept
    OFFER_REJECTED,   // ứng viên reject

    ONBOARDING,       // đang nhập hồ sơ
    HIRED,            // đã tạo employee

    REJECTED
}