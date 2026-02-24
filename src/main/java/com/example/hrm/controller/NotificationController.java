package com.example.hrm.controller;

import com.example.hrm.entity.Notification;
import com.example.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Integer empId = 1;
        long count = notificationService.getUnreadCount(empId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        Integer empId = 1;
        List<Notification> notifications = notificationService.getUnreadNotifications(empId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        Integer empId = 1;
        List<Notification> notifications = notificationService.getAllNotifications(empId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}
