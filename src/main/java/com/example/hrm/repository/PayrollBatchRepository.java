package com.example.hrm.repository;

import com.example.hrm.entity.PayrollBatch;
import com.example.hrm.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollBatchRepository extends JpaRepository<PayrollBatch, Integer> {
    List<PayrollBatch> findByPeriodOrderByIdDesc(PayrollPeriod period);
}
