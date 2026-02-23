package com.example.hrm.repository;

import com.example.hrm.entity.PayslipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayslipItemRepository extends JpaRepository<PayslipItem, Long> {

    List<PayslipItem> findByPayslip_IdOrderByIdAsc(Integer payslipId);

    // ✅ XÓA THEO PAYSLIP (fix lỗi bạn đang gặp)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from PayslipItem it
                where it.payslip.id = :payslipId
            """)
    int deleteByPayslipId(@Param("payslipId") Integer payslipId);

    // ✅ XÓA THEO BATCH (nếu bạn còn dùng chỗ khác)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from PayslipItem it
                where it.payslip.batch.id = :batchId
            """)
    int deleteByBatchId(@Param("batchId") Integer batchId);
}