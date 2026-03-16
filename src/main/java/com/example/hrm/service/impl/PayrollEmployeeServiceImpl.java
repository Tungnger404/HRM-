package com.example.hrm.service.impl;

import com.example.hrm.dto.PayslipDetailDTO;
import com.example.hrm.dto.PayslipItemDTO;
import com.example.hrm.dto.PayslipSummaryDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.entity.PayrollPeriod;
import com.example.hrm.entity.Payslip;
import com.example.hrm.entity.User;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.repository.PayslipItemRepository;
import com.example.hrm.repository.PayslipRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.PayrollEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollEmployeeServiceImpl implements PayrollEmployeeService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final AttendanceLogRepository attendanceRepo;
    private final JobPositionRepository jobRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForEmployee(Integer empId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        if (p.getEmployee() == null || !p.getEmployee().getId().equals(empId)) {
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

        Employee emp = p.getEmployee();

        String jobTitle = "";
        String email = "";
        String phone = "";

        if (emp != null) {
            phone = emp.getPhone() == null ? "" : emp.getPhone();

            if (emp.getJobId() != null) {
                jobTitle = jobRepo.findById(emp.getJobId())
                        .map(JobPosition::getTitle)
                        .orElse("");
            }

            if (emp.getUserId() != null) {
                email = userRepo.findById(emp.getUserId())
                        .map(User::getEmail)
                        .orElse("");
            }
        }

        PayrollPeriod period = p.getBatch() != null ? p.getBatch().getPeriod() : null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (period != null) {
            startDate = period.getStartDate() != null
                    ? period.getStartDate()
                    : LocalDate.of(period.getYear(), period.getMonth(), 1);

            endDate = period.getEndDate() != null
                    ? period.getEndDate()
                    : startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        BigDecimal actualWorkDays = nz(p.getActualWorkDays());
        if (emp != null && startDate != null && endDate != null) {
            long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), startDate, endDate);
            actualWorkDays = BigDecimal.valueOf(actualDaysLong);
        }

        return PayslipDetailDTO.builder()
                .payslipId(p.getId())
                .batchId(p.getBatch().getId())
                .empId(emp != null ? emp.getId() : null)
                .employeeName(empName(emp))
                .period(periodLabel(period))
                .baseSalary(nz(p.getBaseSalary()))
                .standardWorkDays(nz(p.getStandardWorkDays()))
                .actualWorkDays(actualWorkDays)
                .otHours(nz(p.getOtHours()))
                .totalIncome(nz(p.getTotalIncome()))
                .totalDeduction(nz(p.getTotalDeduction()))
                .netSalary(nz(p.getNetSalary()))
                .items(items)
                .jobTitle(jobTitle)
                .email(email)
                .phone(phone)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipSummaryDTO> listEmployeePayslips(Integer empId) {
        return payslipRepo.findReleasedByEmployeeId(empId)
                .stream()
                .map(p -> PayslipSummaryDTO.builder()
                        .payslipId(p.getId())
                        .batchId(p.getBatch().getId())
                        .empId(empId)
                        .employeeName(empName(p.getEmployee()))
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

        return !"REJECTED".equals(slipStatus)
                && ("APPROVED".equals(batchStatus) || "PAID".equals(batchStatus));
    }
}