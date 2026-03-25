package com.example.hrm.service.impl;

import com.example.hrm.entity.Notification;
import com.example.hrm.repository.NotificationRepository;
import com.example.hrm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // ===== Generic create (NEW) =====
    @Override
    @Transactional
    public void create(Integer empId, String type, String title, String message, String linkUrl) {
        notificationRepository.save(Notification.builder()
                .empId(empId)
                .type(type)
                .title(title)
                .message(message)
                .linkUrl(linkUrl)
                .isRead(false)
                .build());
    }

    // ===== Payroll Inquiry (NEW) =====
    @Override
    @Transactional
    public void createPayrollInquirySubmitted(Integer managerEmpId, Integer inquiryId, Integer payslipId, String employeeName) {
        if (managerEmpId == null) return;

        create(
                managerEmpId,
                "PAYROLL_INQUIRY",
                "New payroll inquiry",
                employeeName + " submitted an inquiry for payslip #" + payslipId,
                "/manager/inquiries/" + inquiryId
        );
    }

    @Override
    @Transactional
    public void createPayrollInquiryResolved(Integer empId, Integer inquiryId, Integer payslipId) {
        create(
                empId,
                "PAYROLL_INQUIRY_RESOLVED",
                "Inquiry resolved",
                "Your payroll inquiry has been answered.",
                "/employee/payslips/" + payslipId + "#inquiries"
        );
    }

    // ===== Existing KPI/Evaluation/Training =====
    @Override
    @Transactional
    public void createKpiAssignmentNotification(Integer empId, Integer assignmentId, String hrComment) {
        create(empId, "KPI_ASSIGNED",
                "You have a new KPI assignment",
                hrComment,
                "/evaluation/submit-kpi?assignmentId=" + assignmentId);
    }

    @Override
    @Transactional
    public void createKpiRejectedNotification(Integer empId, Integer assignmentId, String reason) {
        create(empId, "KPI_REJECTED",
                "Your KPI submission needs revision",
                reason,
                "/evaluation/submit-kpi?assignmentId=" + assignmentId);
    }

    @Override
    @Transactional
    public void createEvaluationPendingNotification(Integer managerId, Integer evalId, String employeeName) {
        create(managerId, "EVALUATION_PENDING",
                "New evaluation pending your review",
                employeeName + " has completed self-assessment",
                "/manager/evaluation/review/" + evalId);
    }

    @Override
    @Transactional
    public void createEvaluationCompletedNotification(Integer empId, Integer evalId, String finalScore) {
        create(empId, "EVALUATION_COMPLETED",
                "Your evaluation has been completed",
                "Final Score: " + finalScore,
                "/evaluation/history");
    }

    @Override
    @Transactional
    public void createTrainingRecommendationNotification(Integer empId, String programName) {
        create(empId, "TRAINING_RECOMMENDED",
                "You have been recommended for training",
                "Program: " + programName,
                "/training/recommendations");
    }

    @Override
    @Transactional
    public void createKpiSubmittedNotification(Integer hrStaffId, Integer assignmentId, String employeeName) {
        create(hrStaffId, "KPI_SUBMITTED",
                "New KPI submission to verify",
                employeeName + " has submitted KPI and evidence for review",
                "/hr/kpi/pending-verification");
    }

    // ===== Read/Mark =====
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Integer empId) {
        return notificationRepository.findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(empId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(Integer empId) {
        return notificationRepository.findByEmpIdOrderByCreatedAtDesc(empId);
    }

    @Override
    @Transactional
    public void markAsRead(Integer empId, Integer notificationId) {
        notificationRepository.markAsRead(empId, notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer empId) {
        return notificationRepository.countByEmpIdAndIsReadFalse(empId);
    }
}