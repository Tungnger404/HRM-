package com.example.hrm.repository;

import com.example.hrm.entity.PayrollInquiry;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayrollInquiryRepository extends JpaRepository<PayrollInquiry, Integer> {

    List<PayrollInquiry> findByPayslip_IdOrderByCreatedAtDesc(Integer payslipId);

    @Query("""
                select i from PayrollInquiry i
                join i.employee e
                where e.directManagerId = :managerEmpId
                  and (:status is null or i.status = :status)
                order by i.createdAt desc
            """)
    List<PayrollInquiry> findForManager(@Param("managerEmpId") Integer managerEmpId,
                                        @Param("status") String status);
}
