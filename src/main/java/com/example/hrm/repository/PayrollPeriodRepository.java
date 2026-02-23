package com.example.hrm.repository;

import com.example.hrm.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Integer> {
    List<PayrollPeriod> findAllByOrderByYearDescMonthDesc();
}
