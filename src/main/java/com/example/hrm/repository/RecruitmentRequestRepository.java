package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.entity.RecruitmentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface    RecruitmentRequestRepository
        extends JpaRepository<RecruitmentRequest, Integer> {

    List<RecruitmentRequest> findByCreatedBy(Employee employee);
    @Query("""
        SELECT r
        FROM RecruitmentRequest r
        JOIN FETCH r.department
        JOIN FETCH r.jobPosition
        WHERE r.status <> :status
    """)
    List<RecruitmentRequest> findForHR(
            @Param("status") RecruitmentRequestStatus status
    );
    Optional<RecruitmentRequest> findByReqId(Integer reqId);
}
