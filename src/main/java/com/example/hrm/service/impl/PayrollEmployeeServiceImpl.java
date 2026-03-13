package com.example.hrm.service.impl;

import com.example.hrm.dto.PayslipDetailDTO;
import com.example.hrm.dto.PayslipItemDTO;
import com.example.hrm.dto.PayslipSummaryDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.PayrollPeriod;
import com.example.hrm.entity.Payslip;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.PayslipItemRepository;
import com.example.hrm.repository.PayslipRepository;
import com.example.hrm.service.PayrollEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollEmployeeServiceImpl implements PayrollEmployeeService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForEmployee(Integer empId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        if (!p.getEmployee().getId().equals(empId)) {
            throw new SecurityException("Not allowed");
        }

        if (!canEmployeeView(p)) {
            throw new IllegalStateException("Payslip not released yet");
        }

        List<PayslipItemDTO> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslipId).stream()
                .map(it -> PayslipItemDTO.builder()
                        .id(it.getId())
                        .code(it.getItemCode())
                        .name(it.getItemName())
                        .amount(it.getAmount() == null ? BigDecimal.ZERO : it.getAmount())
                        .type(it.getItemType())
                        .manual(Boolean.TRUE.equals(it.getManualAdjustment()))
                        .build())
                .toList();

        return PayslipDetailDTO.builder()
                .payslipId(p.getId())
                .batchId(p.getBatch().getId())
                .empId(p.getEmployee().getId())
                .employeeName(empName(p.getEmployee()))
                .period(periodLabel(p.getBatch() != null ? p.getBatch().getPeriod() : null))
                .baseSalary(nz(p.getBaseSalary()))
                .standardWorkDays(nz(p.getStandardWorkDays()))
                .actualWorkDays(nz(p.getActualWorkDays()))
                .otHours(nz(p.getOtHours()))
                .totalIncome(nz(p.getTotalIncome()))
                .totalDeduction(nz(p.getTotalDeduction()))
                .netSalary(nz(p.getNetSalary()))
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipSummaryDTO> listEmployeePayslips(Integer empId) {
        Employee emp = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));

        return payslipRepo.findByEmployeeOrderByIdDesc(emp)
                .stream()
                .filter(this::canEmployeeView)
                .map(p -> PayslipSummaryDTO.builder()
                        .payslipId(p.getId())
                        .batchId(p.getBatch().getId())
                        .empId(empId)
                        .employeeName(empName(emp))
                        .period(periodLabel(p.getBatch() != null ? p.getBatch().getPeriod() : null))
                        .totalIncome(nz(p.getTotalIncome()))
                        .totalDeduction(nz(p.getTotalDeduction()))
                        .netSalary(nz(p.getNetSalary()))
                        .build())
                .toList();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String empName(Employee e) {
        if (e == null) return "";
        String n = e.getFullName();
        if (n != null && !n.trim().isEmpty()) return n.trim();
        return "NV" + e.getId();
    }

    private String periodLabel(PayrollPeriod per) {
        if (per == null) return "";

        Integer m = per.getMonth();
        Integer y = per.getYear();

        String mmYY = (m != null && y != null)
                ? String.format("%02d/%d", m, y)
                : (per.getName() != null ? per.getName() : "");

        if (per.getStartDate() != null && per.getEndDate() != null) {
            String s = per.getStartDate().format(VN_DATE);
            String e = per.getEndDate().format(VN_DATE);
            return mmYY + " (" + s + " \u2192 " + e + ")";
        }
        return mmYY;
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