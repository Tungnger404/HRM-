package com.example.hrm.service.impl;

import com.example.hrm.entity.Notification;
import com.example.hrm.repository.NotificationRepository;
import com.example.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void createKpiAssignmentNotification(Integer empId, Integer assignmentId, String hrComment) {
        Notification notification = new Notification();
        notification.setEmpId(empId);
        notification.setType("KPI_ASSIGNED");
        notification.setTitle("You have a new KPI assignment");
        notification.setMessage(hrComment);
        notification.setLinkUrl("/evaluation/submit-kpi");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createKpiRejectedNotification(Integer empId, Integer assignmentId, String reason) {
        Notification notification = new Notification();
        notification.setEmpId(empId);
        notification.setType("KPI_REJECTED");
        notification.setTitle("Your KPI submission needs revision");
        notification.setMessage(reason);
        notification.setLinkUrl("/evaluation/submit-kpi/" + assignmentId + "/edit");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createEvaluationPendingNotification(Integer managerId, Integer evalId, String employeeName) {
        Notification notification = new Notification();
        notification.setEmpId(managerId);
        notification.setType("EVALUATION_PENDING");
        notification.setTitle("New evaluation pending your review");
        notification.setMessage(employeeName + " has completed self-assessment");
        notification.setLinkUrl("/manager/evaluation/review/" + evalId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createEvaluationCompletedNotification(Integer empId, Integer evalId, String finalScore) {
        Notification notification = new Notification();
        notification.setEmpId(empId);
        notification.setType("EVALUATION_COMPLETED");
        notification.setTitle("Your evaluation has been completed");
        notification.setMessage("Final Score: " + finalScore);
        notification.setLinkUrl("/evaluation/history");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createTrainingRecommendationNotification(Integer empId, String programName) {
        Notification notification = new Notification();
        notification.setEmpId(empId);
        notification.setType("TRAINING_RECOMMENDED");
        notification.setTitle("You have been recommended for training");
        notification.setMessage("Program: " + programName);
        notification.setLinkUrl("/training/recommendations");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(Integer empId) {
        return notificationRepository.findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(empId);
    }

    @Override
    public List<Notification> getAllNotifications(Integer empId) {
        return notificationRepository.findByEmpIdOrderByCreatedAtDesc(empId);
    }

    @Override
    public void markAsRead(Integer notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public long getUnreadCount(Integer empId) {
        return notificationRepository.countByEmpIdAndIsReadFalse(empId);
    }
}
