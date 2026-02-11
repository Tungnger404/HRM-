package com.example.hrm.repository;

import com.example.hrm.entity.LeaveOrOtRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RequestRepository extends JpaRepository<LeaveOrOtRequest, Integer> {

    // SQL Server dùng DATEDIFF
    @Query(value = """
        select coalesce(sum(datediff(minute, start_time, end_time)), 0)
        from requests
        where emp_id = :empId
          and request_type = 'OVERTIME'
          and status = 'APPROVED'
          and start_time >= :start
          and end_time <= :end
    """, nativeQuery = true)
    long sumApprovedOvertimeMinutes(@Param("empId") Integer empId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
