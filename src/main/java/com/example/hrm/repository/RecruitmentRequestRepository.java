package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.entity.RecruitmentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecruitmentRequestRepository
        extends JpaRepository<RecruitmentRequest, Integer> {

    // Request theo employee
    List<RecruitmentRequest> findByCreatedBy(Employee employee);

    // HR xem tất cả request
    @Query("""
        SELECT r
        FROM RecruitmentRequest r
        JOIN FETCH r.department
        JOIN FETCH r.jobPosition
        ORDER BY r.createdAt DESC
    """)
    List<RecruitmentRequest> findForHR();

    // tìm theo reqId
    Optional<RecruitmentRequest> findByReqId(Integer reqId);

    // request của employee
    List<RecruitmentRequest> findByCreatedBy_EmpId(Integer empId);

    // filter theo status
    List<RecruitmentRequest> findByStatus(RecruitmentRequestStatus status);

    // detail request
    @Query("""
        SELECT r
        FROM RecruitmentRequest r
        JOIN FETCH r.department
        JOIN FETCH r.jobPosition
        WHERE r.reqId = :id
    """)
    Optional<RecruitmentRequest> findByIdWithDetails(@Param("id") Integer id);


    // SEARCH + FILTER
    @Query("""
        SELECT r
        FROM RecruitmentRequest r
        JOIN FETCH r.department
        JOIN FETCH r.jobPosition
        WHERE
            (:keyword IS NULL
                OR LOWER(r.jobPosition.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.department.deptName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND
            (:status IS NULL OR r.status = :status)
        ORDER BY r.createdAt DESC
    """)
    List<RecruitmentRequest> searchRequests(
            @Param("keyword") String keyword,
            @Param("status") RecruitmentRequestStatus status
    );
}