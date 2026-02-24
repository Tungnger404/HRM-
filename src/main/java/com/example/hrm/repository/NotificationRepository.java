package com.example.hrm.repository;

import com.example.hrm.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(Integer empId);
    
    List<Notification> findByEmpIdOrderByCreatedAtDesc(Integer empId);
    
    long countByEmpIdAndIsReadFalse(Integer empId);
}
