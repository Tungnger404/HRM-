package com.example.hrm.repository;

import com.example.hrm.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    // Tìm lịch sử điểm danh của 1 nhân viên
    List<AttendanceLog> findByEmpId(Integer empId);
}