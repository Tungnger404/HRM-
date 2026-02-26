package com.example.hrm.service;

import com.example.hrm.entity.Notification;
import java.util.List;

public interface NotificationService {

    // ===== existing KPI/eval/training =====
    void createKpiAssignmentNotification(Integer empId, Integer assignmentId, String hrComment);
    
    void createKpiRejectedNotification(Integer empId, Integer assignmentId, String reason);
    
    void createEvaluationPendingNotification(Integer managerId, Integer evalId, String employeeName);
    
    void createEvaluationCompletedNotification(Integer empId, Integer evalId, String finalScore);
    
    void createTrainingRecommendationNotification(Integer empId, String programName);

    // ===== NEW: KPI submission notification for HR =====
    void createKpiSubmittedNotification(Integer hrStaffId, Integer assignmentId, String employeeName);

    // ===== NEW: generic helper (optional but rất tiện) =====
    void create(Integer empId, String type, String title, String message, String linkUrl);

    // ===== NEW: Payroll Inquiry notifications =====
    void createPayrollInquirySubmitted(Integer managerEmpId, Integer inquiryId, Integer payslipId, String employeeName);
    void createPayrollInquiryResolved(Integer empId, Integer inquiryId, Integer payslipId);

    // ===== read/mark =====
    List<Notification> getUnreadNotifications(Integer empId);

    List<Notification> getAllNotifications(Integer empId);

    // nên mark theo empId để không ai mark hộ
    void markAsRead(Integer empId, Integer notificationId);

    long getUnreadCount(Integer empId);
}
