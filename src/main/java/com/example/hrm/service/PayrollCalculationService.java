package com.example.hrm.service;

import com.example.hrm.entity.PayrollBatch;
import com.example.hrm.entity.Payslip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PayrollCalculationService {

    record PayslipComputation(
            BigDecimal salaryByDays,
            BigDecimal overtimePay,
            BigDecimal benefitIncome,
            BigDecimal benefitDeduction,
            BigDecimal totalIncome,
            BigDecimal totalDeduction,
            BigDecimal netSalary,
            List<BenefitService.BenefitApplied> benefits
    ) {}

    PayslipComputation compute(Integer empId,
                               LocalDate startDate,
                               LocalDate endDate,
                               BigDecimal baseSalary,
                               BigDecimal standardDays,
                               BigDecimal actualDays,
                               BigDecimal otHours);

    void upsertComputedItems(Payslip payslip, PayslipComputation computation, boolean manualAdjustment);

    void recalcPayslipTotalsFromItems(Payslip payslip);

    void recalcBatchTotals(PayrollBatch batch);
}