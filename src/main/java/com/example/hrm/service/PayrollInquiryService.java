package com.example.hrm.service;

import com.example.hrm.dto.PayrollInquiryCreateDTO;
import com.example.hrm.dto.PayrollInquiryDTO;

import java.util.List;

public interface PayrollInquiryService {

    List<PayrollInquiryDTO> listAllInquiriesForEmployee(Integer empId, String status);

    List<PayrollInquiryDTO> listInquiriesForEmployee(Integer empId, Integer payslipId);

    PayrollInquiryDTO submitInquiry(Integer empId, PayrollInquiryCreateDTO req);

    List<PayrollInquiryDTO> listInquiriesForManager(Integer managerEmpId, String status);

    List<PayrollInquiryDTO> listInquiriesForHr(String status);

    PayrollInquiryDTO getInquiry(Integer inquiryId);

    void resolveInquiry(Integer managerEmpId, Integer inquiryId, String answer);

    void resolveInquiryByHr(Integer inquiryId, String answer);
}