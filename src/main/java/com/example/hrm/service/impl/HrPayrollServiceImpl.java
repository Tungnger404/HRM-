package com.example.hrm.service.impl;

import com.example.hrm.dto.HrPayrollReviewDTO;
import com.example.hrm.dto.PayrollPeriodSummaryDTO;
import com.example.hrm.dto.PayrollRowDTO;
import com.example.hrm.dto.PayslipDetailDTO;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.BenefitService;
import com.example.hrm.service.HrPayrollService;
import com.example.hrm.service.NotificationService;
import com.example.hrm.service.PayrollCalculationService;
import com.example.hrm.service.PayrollManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class HrPayrollServiceImpl implements HrPayrollService {

    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final PayrollBatchRepository batchRepo;
    private final EmployeeRepository employeeRepo;
    private final AttendanceLogRepository attendanceRepo;
    private final RequestRepository requestRepo;
    private final ContractRepository contractRepo;
    private final JobPositionRepository jobRepo;
    private final BankAccountRepository bankAccountRepo;
    private final BenefitService benefitService;
    private final NotificationService notificationService;
    private final PayrollCalculationService payrollCalculationService;
    private final PayrollManagerService payrollManagerService;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriodSummaryDTO> listPayrollPeriods() {
        return payrollManagerService.listPayrollPeriods();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRowDTO> listRejectedPayrollRowsForHr(String q, Integer periodId) {
        return payrollManagerService.listPayrollRowsForManager(null, q, "REJECTED", periodId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRowDTO> listAllPayrollRows(String q, String status, Integer periodId) {
        return payrollManagerService.listPayrollRowsForManager(null, q, status, periodId);
    }

    @Override
    @Transactional(readOnly = true)
    public HrPayrollReviewDTO getRejectedPayslipForHr(Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        if (!"REJECTED".equalsIgnoreCase(p.getSlipStatus())) {
            throw new IllegalStateException("Payslip này chưa ở trạng thái REJECTED.");
        }

        PayrollPeriod period = p.getBatch().getPeriod();
        LocalDate startDate = period.getStartDate() != null
                ? period.getStartDate()
                : LocalDate.of(period.getYear(), period.getMonth(), 1);

        LocalDate endDate = period.getEndDate() != null
                ? period.getEndDate()
                : startDate.withDayOfMonth(startDate.lengthOfMonth());

        Employee emp = p.getEmployee();

        BigDecimal standardDays = nz(p.getStandardWorkDays());
        BigDecimal savedBaseSalary = nz(p.getBaseSalary());
        BigDecimal savedOtHours = nz(p.getOtHours());

        long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), startDate, endDate);
        BigDecimal actualDays = BigDecimal.valueOf(actualDaysLong);

        long otMinutes = requestRepo.sumApprovedOvertimeMinutes(
                emp.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        BigDecimal recalculatedOtHours = BigDecimal.valueOf(otMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal contractBaseSalary = contractRepo.findActiveContractForPeriod(emp.getId(), startDate, endDate)
                .map(Contract::getBaseSalary)
                .orElse(BigDecimal.ZERO);

        PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                emp.getId(),
                startDate,
                endDate,
                savedBaseSalary,
                standardDays,
                actualDays,
                recalculatedOtHours
        );

        BigDecimal missingWorkDays = standardDays.subtract(actualDays);
        if (missingWorkDays.compareTo(BigDecimal.ZERO) < 0) {
            missingWorkDays = BigDecimal.ZERO;
        }

        boolean bankMissing = bankAccountRepo.findFirstByEmpIdAndIsPrimaryTrue(emp.getId()).isEmpty();

        boolean newHireInPeriod = emp.getJoinDate() != null
                && !emp.getJoinDate().isBefore(startDate)
                && !emp.getJoinDate().isAfter(endDate);

        BigDecimal suggestedProratedBase = BigDecimal.ZERO;
        if (standardDays.compareTo(BigDecimal.ZERO) > 0) {
            suggestedProratedBase = savedBaseSalary
                    .multiply(actualDays)
                    .divide(standardDays, 2, RoundingMode.HALF_UP);
        }

        boolean baseSalaryMismatch = contractBaseSalary.compareTo(BigDecimal.ZERO) > 0
                && savedBaseSalary.compareTo(contractBaseSalary) != 0;

        boolean otMismatch = savedOtHours.compareTo(recalculatedOtHours) != 0;
        boolean deductionMismatch = nz(p.getTotalDeduction()).compareTo(nz(c.totalDeduction())) != 0;

        String jobTitle = "";
        if (emp.getJobId() != null) {
            jobTitle = jobRepo.findById(emp.getJobId())
                    .map(JobPosition::getTitle)
                    .orElse("");
        }

        return HrPayrollReviewDTO.builder()
                .payslipId(p.getId())
                .batchId(p.getBatch() != null ? p.getBatch().getId() : null)
                .empId(emp.getId())
                .employeeName(empName(emp))
                .jobTitle(jobTitle)
                .periodLabel(String.format("%02d/%d", period.getMonth(), period.getYear()))
                .batchStatus(p.getBatch() != null ? p.getBatch().getStatus() : "")
                .slipStatus(p.getSlipStatus())
                .rejectReason(p.getRejectReason())
                .rejectedAt(p.getRejectedAt())
                .periodStart(startDate)
                .periodEnd(endDate)
                .joinDate(emp.getJoinDate())
                .savedBaseSalary(savedBaseSalary)
                .contractBaseSalary(contractBaseSalary)
                .standardWorkDays(standardDays)
                .actualWorkDays(actualDays)
                .missingWorkDays(missingWorkDays)
                .savedOtHours(savedOtHours)
                .recalculatedOtHours(recalculatedOtHours)
                .savedIncome(nz(p.getTotalIncome()))
                .savedDeduction(nz(p.getTotalDeduction()))
                .savedNet(nz(p.getNetSalary()))
                .recalculatedIncome(nz(c.totalIncome()))
                .recalculatedDeduction(nz(c.totalDeduction()))
                .recalculatedNet(nz(c.netSalary()))
                .suggestedProratedBase(suggestedProratedBase)
                .bankMissing(bankMissing)
                .newHireInPeriod(newHireInPeriod)
                .missingAttendance(actualDays.compareTo(standardDays) < 0)
                .baseSalaryMismatch(baseSalaryMismatch)
                .otMismatch(otMismatch)
                .deductionMismatch(deductionMismatch)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForHr(Integer payslipId) {
        return payrollManagerService.getPayslipDetailForManager(null, payslipId);
    }

    @Override
    public void updateRejectedPayslipBaseSalary(Integer payslipId, BigDecimal newBaseSalary) {
        if (newBaseSalary == null || newBaseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base salary invalid");
        }

        Payslip p = requireRejectedPayslip(payslipId);

        LocalDate[] range = resolvePeriodRange(p);
        BigDecimal base = newBaseSalary.setScale(2, RoundingMode.HALF_UP);

        PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                p.getEmployee() != null ? p.getEmployee().getId() : null,
                range[0],
                range[1],
                base,
                nz(p.getStandardWorkDays()),
                nz(p.getActualWorkDays()),
                nz(p.getOtHours())
        );

        p.setBaseSalary(base);
        payslipRepo.save(p);

        upsertRejectedComputedItems(p, c);
        payrollCalculationService.recalcPayslipTotalsFromItems(p);
        payrollCalculationService.recalcBatchTotals(p.getBatch());
    }

    @Override
    public void addActiveBenefitToRejectedPayslip(Integer payslipId, Integer benefitId) {
        Payslip p = requireRejectedPayslip(payslipId);
        Benefit b = benefitService.requireActive(benefitId);

        BigDecimal base = nz(p.getBaseSalary());
        BigDecimal amount = "PERCENT_BASE".equalsIgnoreCase(b.getCalcMethod())
                ? base.multiply(nz(b.getValue())).setScale(2, RoundingMode.HALF_UP)
                : nz(b.getValue()).setScale(2, RoundingMode.HALF_UP);

        String codeKey = b.getCode() == null ? "" : b.getCode().trim().toUpperCase();
        if (codeKey.isBlank()) {
            throw new IllegalStateException("Benefit code is empty");
        }

        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslipId);
        PayslipItem item = items.stream()
                .filter(x -> x.getItemCode() != null && x.getItemCode().trim().equalsIgnoreCase(codeKey))
                .findFirst()
                .orElse(null);

        if (item == null) {
            item = PayslipItem.builder()
                    .payslip(p)
                    .itemCode(codeKey)
                    .itemName(b.getName())
                    .itemType(b.getType())
                    .amount(amount)
                    .manualAdjustment(true)
                    .build();
        } else {
            item.setItemName(b.getName());
            item.setItemType(b.getType());
            item.setAmount(amount);
            item.setManualAdjustment(true);
        }

        itemRepo.save(item);
        payrollCalculationService.recalcPayslipTotalsFromItems(p);
        payrollCalculationService.recalcBatchTotals(p.getBatch());
    }

    @Override
    public void reopenPayslipByHr(Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        if (!"REJECTED".equalsIgnoreCase(p.getSlipStatus())) {
            throw new IllegalStateException("Payslip này không ở trạng thái REJECTED.");
        }

        p.setSlipStatus("ACTIVE");
        p.setSentToEmployee(false);
        p.setRejectReason(null);
        p.setRejectedBy(null);
        p.setRejectedAt(null);
        payslipRepo.save(p);

        PayrollBatch batch = p.getBatch();
        if (batch != null && !"DRAFT".equalsIgnoreCase(batch.getStatus())) {
            batch.setStatus("DRAFT");
            batchRepo.save(batch);
        }

        payrollCalculationService.recalcBatchTotals(p.getBatch());

        Integer managerEmpId = p.getEmployee() != null ? p.getEmployee().getDirectManagerId() : null;
        if (managerEmpId != null) {
            notificationService.create(
                    managerEmpId,
                    "PAYROLL_REOPENED",
                    "HR reopened rejected payslip",
                    empName(p.getEmployee()) + " has been fixed and reopened for review.",
                    "/manager/payroll/payslips/" + p.getId()
            );
        }
    }

    private Payslip requireRejectedPayslip(Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        if (!"REJECTED".equalsIgnoreCase(p.getSlipStatus())) {
            throw new IllegalStateException("Chỉ sửa được payslip đang REJECTED.");
        }

        String batchStatus = p.getBatch() == null || p.getBatch().getStatus() == null
                ? ""
                : p.getBatch().getStatus().trim().toUpperCase();
        if ("PAID".equals(batchStatus)) {
            throw new IllegalStateException("Batch PAID không cho sửa.");
        }
        return p;
    }

    private void upsertRejectedComputedItems(Payslip payslip,
                                             PayrollCalculationService.PayslipComputation computation) {
        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslip.getId());

        Map<String, PayslipItem> byCode = new LinkedHashMap<>();
        for (PayslipItem it : items) {
            String code = it.getItemCode() == null ? "" : it.getItemCode().trim().toUpperCase();
            if (!code.isBlank()) {
                byCode.put(code, it);
            }
        }

        upsertItem(byCode, payslip, "BASE_BY_DAYS", "Salary by work days",
                computation.salaryByDays(), "INCOME", false);
        upsertItem(byCode, payslip, "OT_PAY", "Overtime pay",
                computation.overtimePay(), "INCOME", false);

        Set<String> keep = new HashSet<>();
        keep.add("BASE_BY_DAYS");
        keep.add("OT_PAY");

        for (BenefitService.BenefitApplied benefit : computation.benefits()) {
            String code = benefit.code() == null ? "" : benefit.code().trim().toUpperCase();
            if (code.isBlank()) continue;
            keep.add(code);
            upsertItem(byCode, payslip, code, benefit.name(), benefit.amount(), benefit.type(), false);
        }

        List<PayslipItem> toDelete = items.stream()
                .filter(it -> !Boolean.TRUE.equals(it.getManualAdjustment()))
                .filter(it -> {
                    String code = it.getItemCode() == null ? "" : it.getItemCode().trim().toUpperCase();
                    return !code.isBlank() && !keep.contains(code);
                })
                .toList();

        if (!toDelete.isEmpty()) {
            itemRepo.deleteAll(toDelete);
            for (PayslipItem it : toDelete) {
                String code = it.getItemCode() == null ? "" : it.getItemCode().trim().toUpperCase();
                byCode.remove(code);
            }
        }

        itemRepo.saveAll(byCode.values());
    }

    private void upsertItem(Map<String, PayslipItem> byCode,
                            Payslip payslip,
                            String code,
                            String name,
                            BigDecimal amount,
                            String type,
                            boolean manualAdjustment) {
        String key = code.trim().toUpperCase();
        PayslipItem item = byCode.get(key);

        if (item == null) {
            item = PayslipItem.builder()
                    .payslip(payslip)
                    .itemCode(key)
                    .itemName(name)
                    .itemType(type)
                    .manualAdjustment(manualAdjustment)
                    .amount(BigDecimal.ZERO)
                    .build();
            byCode.put(key, item);
        }

        item.setItemCode(key);
        item.setItemName(name);
        item.setItemType(type);
        item.setAmount(amount == null ? BigDecimal.ZERO : amount);
        item.setManualAdjustment(manualAdjustment);
    }

    private LocalDate[] resolvePeriodRange(Payslip payslip) {
        PayrollPeriod period = payslip.getBatch() != null ? payslip.getBatch().getPeriod() : null;
        if (period == null) {
            throw new IllegalStateException("Payslip has no payroll period.");
        }

        LocalDate start = period.getStartDate() != null
                ? period.getStartDate()
                : LocalDate.of(period.getYear(), period.getMonth(), 1);
        LocalDate end = period.getEndDate() != null
                ? period.getEndDate()
                : start.withDayOfMonth(start.lengthOfMonth());
        return new LocalDate[]{start, end};
    }

    private String empName(Employee emp) {
        return emp == null ? "" : Objects.toString(emp.getFullName(), "");
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}