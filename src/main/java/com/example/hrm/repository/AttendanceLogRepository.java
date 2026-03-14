package com.example.hrm.repository;

import com.example.hrm.entity.AttendanceLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    Optional<AttendanceLog> findByEmployee_EmpIdAndWorkDate(Integer empId, LocalDate workDate);

    Optional<AttendanceLog> findTopByEmployee_EmpIdAndCheckOutIsNullOrderByWorkDateDesc(Integer empId);

    List<AttendanceLog> findByEmployee_EmpIdOrderByWorkDateDesc(Integer empId);
}