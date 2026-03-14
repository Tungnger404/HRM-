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
                  and (:status is null or :status = '' or i.status = :status)
                order by i.createdAt desc
            """)
    List<PayrollInquiry> findForManager(@Param("managerEmpId") Integer managerEmpId,
                                        @Param("status") String status);

    @Query("""
                select i from PayrollInquiry i
                where (:status is null or :status = '' or i.status = :status)
                order by i.createdAt desc
            """)
    List<PayrollInquiry> findForHr(@Param("status") String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from PayrollInquiry i
                where i.payslip.batch.id = :batchId
            """)
    int deleteByBatchId(@Param("batchId") Integer batchId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PayrollInquiry i where i.payslip.id = :payslipId")
    int deleteByPayslipId(@Param("payslipId") Integer payslipId);

    @Query("""
    select i from PayrollInquiry i
    where i.employee.id = :empId
      and (:payslipId is null or i.payslip.id = :payslipId)
      and (:status is null or :status = '' or i.status = :status)
    order by i.createdAt desc
""")
    List<PayrollInquiry> findForEmployee(@Param("empId") Integer empId,
                                         @Param("payslipId") Integer payslipId,
                                         @Param("status") String status);
}