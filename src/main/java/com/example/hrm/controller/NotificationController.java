package com.example.hrm.controller;

import com.example.hrm.entity.Notification;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
        long count = notificationService.getUnreadCount(empId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(empId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications(Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
        return ResponseEntity.ok(notificationService.getAllNotifications(empId));
    }

    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId, Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
        notificationService.markAsRead(empId, notificationId);
        return ResponseEntity.ok().build();
    }
}