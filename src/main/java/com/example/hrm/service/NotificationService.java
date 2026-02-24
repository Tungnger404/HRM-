package com.example.hrm.service;

import com.example.hrm.entity.Notification;
import java.util.List;

public interface NotificationService {
    
    void createKpiAssignmentNotification(Integer empId, Integer assignmentId, String hrComment);
    
    void createKpiRejectedNotification(Integer empId, Integer assignmentId, String reason);
    
    void createEvaluationPendingNotification(Integer managerId, Integer evalId, String employeeName);
    
    void createEvaluationCompletedNotification(Integer empId, Integer evalId, String finalScore);
    
    void createTrainingRecommendationNotification(Integer empId, String programName);
    
    List<Notification> getUnreadNotifications(Integer empId);
    
    List<Notification> getAllNotifications(Integer empId);
    
    void markAsRead(Integer notificationId);
    
    long getUnreadCount(Integer empId);
}
