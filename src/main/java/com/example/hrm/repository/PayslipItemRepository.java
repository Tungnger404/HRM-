package com.example.hrm.repository;

import com.example.hrm.entity.PayslipItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayslipItemRepository extends JpaRepository<PayslipItem, Long> {
    List<PayslipItem> findByPayslip_IdOrderByIdAsc(Integer payslipId);
}
