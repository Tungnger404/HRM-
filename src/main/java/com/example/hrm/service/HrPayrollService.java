package com.example.hrm.service;

import com.example.hrm.dto.HrPayrollReviewDTO;
import com.example.hrm.dto.PayrollPeriodSummaryDTO;
import com.example.hrm.dto.PayrollRowDTO;
import com.example.hrm.dto.PayslipDetailDTO;

import java.math.BigDecimal;
import java.util.List;

public interface HrPayrollService {

    List<PayrollPeriodSummaryDTO> listPayrollPeriods();

    List<PayrollRowDTO> listRejectedPayrollRowsForHr(String q, Integer periodId);

    List<PayrollRowDTO> listAllPayrollRows(String q, String status, Integer periodId);

    HrPayrollReviewDTO getRejectedPayslipForHr(Integer payslipId);

    PayslipDetailDTO getPayslipDetailForHr(Integer payslipId);

    void updateRejectedPayslipBaseSalary(Integer payslipId, BigDecimal newBaseSalary);

    void addActiveBenefitToRejectedPayslip(Integer payslipId, Integer benefitId);

    void reopenPayslipByHr(Integer payslipId);
}