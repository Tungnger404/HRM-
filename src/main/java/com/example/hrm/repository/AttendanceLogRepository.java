package com.example.hrm.repository;

import com.example.hrm.entity.AttendanceLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    @Query("""
        select count(a) from AttendanceLog a
        where a.employee.empId = :empId
          and a.workDate between :start and :end
          and (a.status is null or a.status <> 'ABSENT')
    """)
    long countActualWorkDays(@Param("empId") Integer empId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    // ✅ lịch sử điểm danh theo nhân viên
    List<AttendanceLog> findByEmployee_EmpId(Integer empId);
}
