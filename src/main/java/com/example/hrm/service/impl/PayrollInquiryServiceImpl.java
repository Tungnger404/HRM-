package com.example.hrm.service.impl;

import com.example.hrm.dto.PayrollInquiryCreateDTO;
import com.example.hrm.dto.PayrollInquiryDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.PayrollInquiry;
import com.example.hrm.entity.Payslip;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.PayrollInquiryRepository;
import com.example.hrm.repository.PayslipRepository;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.PayrollInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollInquiryServiceImpl implements PayrollInquiryService {

    private final PayrollInquiryRepository inquiryRepo;
    private final PayslipRepository payslipRepo;
    private final EmployeeRepository employeeRepo;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listAllInquiriesForEmployee(Integer empId, String status) {
        return inquiryRepo.findForEmployee(empId, null, status).stream()
                .map(i -> PayrollInquiryDTO.builder()
                        .id(i.getId())
                        .payslipId(i.getPayslip().getId())
                        .empId(empId)
                        .question(i.getQuestion())
                        .answer(i.getAnswer())
                        .status(i.getStatus())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listInquiriesForEmployee(Integer empId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        if (!p.getEmployee().getId().equals(empId)) {
            throw new SecurityException("Not allowed");
        }

        if (!canEmployeeView(p)) {
            throw new IllegalStateException("Payslip not released yet");
        }

        return inquiryRepo.findByPayslip_IdOrderByCreatedAtDesc(payslipId).stream()
                .filter(i -> i.getEmployee() != null && i.getEmployee().getId().equals(empId))
                .map(i -> PayrollInquiryDTO.builder()
                        .id(i.getId())
                        .payslipId(payslipId)
                        .empId(empId)
                        .question(i.getQuestion())
                        .answer(i.getAnswer())
                        .status(i.getStatus())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public PayrollInquiryDTO submitInquiry(Integer empId, PayrollInquiryCreateDTO req) {
        Payslip p = payslipRepo.findById(req.getPayslipId())
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        if (!p.getEmployee().getId().equals(empId)) {
            throw new SecurityException("Not allowed");
        }

        if (!Boolean.TRUE.equals(p.getSentToEmployee())) {
            throw new IllegalStateException("Payslip not released yet");
        }

        Employee emp = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        PayrollInquiry inq = PayrollInquiry.builder()
                .payslip(p)
                .employee(emp)
                .question(req.getQuestion())
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        inq = inquiryRepo.save(inq);

        notificationService.createPayrollInquirySubmitted(
                emp.getDirectManagerId(),
                inq.getId(),
                p.getId(),
                empName(emp)
        );

        return PayrollInquiryDTO.builder()
                .id(inq.getId())
                .payslipId(p.getId())
                .empId(empId)
                .question(inq.getQuestion())
                .answer(inq.getAnswer())
                .status(inq.getStatus())
                .createdAt(inq.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listInquiriesForManager(Integer managerEmpId, String status) {
        return inquiryRepo.findForManager(managerEmpId, status).stream()
                .map(i -> PayrollInquiryDTO.builder()
                        .id(i.getId())
                        .payslipId(i.getPayslip().getId())
                        .empId(i.getEmployee().getId())
                        .employeeName(empName(i.getEmployee()))
                        .question(i.getQuestion())
                        .answer(i.getAnswer())
                        .status(i.getStatus())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollInquiryDTO getInquiry(Integer inquiryId) {
        PayrollInquiry i = inquiryRepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        return PayrollInquiryDTO.builder()
                .id(i.getId())
                .payslipId(i.getPayslip().getId())
                .empId(i.getEmployee().getId())
                .employeeName(empName(i.getEmployee()))
                .question(i.getQuestion())
                .answer(i.getAnswer())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .build();
    }

    @Override
    public void resolveInquiry(Integer managerEmpId, Integer inquiryId, String answer) {
        PayrollInquiry i = inquiryRepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        Integer direct = i.getEmployee().getDirectManagerId();
        if (direct == null || !direct.equals(managerEmpId)) {
            throw new SecurityException("Not allowed to resolve this inquiry");
        }

        i.setAnswer(answer);
        i.setStatus("RESOLVED");
        inquiryRepo.save(i);

        notificationService.createPayrollInquiryResolved(
                i.getEmployee().getId(),
                i.getId(),
                i.getPayslip().getId()
        );
    }

    private String empName(Employee e) {
        if (e == null) return "";
        String n = e.getFullName();
        if (n != null && !n.trim().isEmpty()) return n.trim();
        return "NV" + e.getId();
    }

    private boolean canEmployeeView(Payslip p) {
        if (p == null) return false;

        String batchStatus = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? ""
                : p.getBatch().getStatus().trim().toUpperCase();

        String slipStatus = p.getSlipStatus() == null
                ? "ACTIVE"
                : p.getSlipStatus().trim().toUpperCase();

        return Boolean.TRUE.equals(p.getSentToEmployee())
                && !"REJECTED".equals(slipStatus)
                && ("APPROVED".equals(batchStatus) || "PAID".equals(batchStatus));
    }
}