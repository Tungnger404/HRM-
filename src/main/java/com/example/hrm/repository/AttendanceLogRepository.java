package com.example.hrm.repository;

import com.example.hrm.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    @Query("""
        select count(a) from AttendanceLog a
        where a.employee.id = :empId
          and a.workDate between :start and :end
          and a.status <> 'ABSENT'
    """)
    long countActualWorkDays(Integer empId, LocalDate start, LocalDate end);
}
