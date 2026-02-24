package com.example.hrm.repository;

import com.example.hrm.entity.Notification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(Integer empId);

    List<Notification> findByEmpIdOrderByCreatedAtDesc(Integer empId);

    long countByEmpIdAndIsReadFalse(Integer empId);

    @Modifying
    @Query("update Notification n set n.isRead = true where n.notificationId = :id and n.empId = :empId")
    int markAsRead(@Param("empId") Integer empId, @Param("id") Integer id);
}