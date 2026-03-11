package com.example.hrm.service;

import java.util.List;

public interface PayrollExportService {

    byte[] exportBatchExcel(Integer batchId);

    byte[] exportBankTransferExcel(List<Integer> batchIds);

    byte[] buildPayslipPdf(Integer empId, Integer payslipId);
}