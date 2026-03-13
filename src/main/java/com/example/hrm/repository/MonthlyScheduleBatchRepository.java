package com.example.hrm.repository;

import com.example.hrm.entity.MonthlyScheduleBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthlyScheduleBatchRepository extends JpaRepository<MonthlyScheduleBatch, Long> {
    List<MonthlyScheduleBatch> findAllByOrderByScheduleMonthDescBatchIdDesc();
}