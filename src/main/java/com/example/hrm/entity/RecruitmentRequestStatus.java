package com.example.hrm.entity;

public enum RecruitmentRequestStatus {
    DRAFT,      // Manager đang soạn
    SUBMITTED,  // Chờ duyệt
    APPROVED,   // Đã duyệt (Sẵn sàng để HR tạo JD)
    REJECTED,   // Bị từ chối
    CLOSED      // Đã hoàn thành tuyển dụng
}

