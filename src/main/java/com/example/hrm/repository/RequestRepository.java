package com.example.hrm.repository;

import com.example.hrm.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    @Query("""
                select coalesce(sum(timestampdiff(minute, r.startTime, r.endTime)), 0)
                from Request r
                where r.employee.id = :empId
                  and r.requestType = 'OVERTIME'
                  and r.status = 'APPROVED'
                  and r.startTime >= :start
                  and r.endTime <= :end
            """)
    long sumApprovedOvertimeMinutes(Integer empId, LocalDateTime start, LocalDateTime end);

}
