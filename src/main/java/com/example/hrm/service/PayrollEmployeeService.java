package com.example.hrm.service;

import com.example.hrm.dto.PayslipDetailDTO;
import com.example.hrm.dto.PayslipSummaryDTO;

import java.util.List;

public interface PayrollEmployeeService {

    PayslipDetailDTO getPayslipDetailForEmployee(Integer empId, Integer payslipId);

    List<PayslipSummaryDTO> listEmployeePayslips(Integer empId);
}