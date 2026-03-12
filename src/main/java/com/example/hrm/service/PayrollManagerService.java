package com.example.hrm.service;

import com.example.hrm.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface PayrollManagerService {
    Integer createPayrollPeriod(Integer month, Integer year);

    record SalaryUpdateResult(BigDecimal baseSalary, BigDecimal netSalary, String slipStatus) {}

    void addActiveBenefitToPayslip(Integer managerEmpId, Integer payslipId, Integer benefitId);

    void approvePayslip(Integer managerEmpId, Integer payslipId);

    SalaryUpdateResult updatePayslipBaseSalary(Integer managerEmpId,
                                               Integer payslipId,
                                               BigDecimal newBaseSalary);

    List<Integer> findBatchIdsByPayslipIds(List<Integer> payslipIds);

    void rejectPayslip(Integer payslipId);

    List<PayrollRowDTO> listPayrollRowsForManager(Integer managerEmpId, String q, String status, Integer periodId);

    List<PayrollPeriodSummaryDTO> listPayrollPeriods();

    List<PayrollBatchSummaryDTO> listBatchesByPeriod(Integer periodId);

    List<EmployeeSearchResultDTO> searchEmployeesForBatch(Integer batchId, String keyword);

    PayrollBatchDetailDTO viewBatchDetail(Integer batchId);

    PayslipDetailDTO getPayslipDetailForManager(Integer managerEmpId, Integer payslipId);

    Integer generatePayrollDraft(Integer periodId, Integer createdByEmpId);

    void submitBatchForApproval(Integer batchId);

    void approveBatch(Integer batchId, Integer approverEmpId);

    void rejectBatch(Integer batchId);

    List<PayrollBatchSummaryDTO> listDraftBatches();
    Integer addEmployeeToPayroll(Integer batchId, Integer empId, BigDecimal baseSalary);
}