package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Integer> {
    List<Payslip> findByEmployeeOrderByIdDesc(Employee employee);
    List<Payslip> findByBatch_IdOrderByIdAsc(Integer batchId);
}
