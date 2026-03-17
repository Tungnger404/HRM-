package com.example.hrm.service.impl;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.BenefitService;
import com.example.hrm.service.PayrollCalculationService;
import com.example.hrm.service.PayrollManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.service.NotificationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollManagerServiceImpl implements PayrollManagerService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PayrollPeriodRepository periodRepo;
    private final PayrollBatchRepository batchRepo;
    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final PayrollInquiryRepository inquiryRepo;
    private final EmployeeRepository employeeRepo;
    private final ContractRepository contractRepo;
    private final AttendanceLogRepository attendanceRepo;
    private final RequestRepository requestRepo;
    private final BenefitService benefitService;
    private final BankAccountRepository bankAccountRepo;
    private final PayrollCalculationService payrollCalculationService;
    private final UserRepository userRepo;
    private final JobPositionRepository jobRepo;

    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepo;

    @Override
    public void addActiveBenefitToPayslip(Integer managerEmpId, Integer payslipId, Integer benefitId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        validatePayslipAccess(p, managerEmpId, "Not allowed to edit this payslip");

        String batchStatus = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? "" : p.getBatch().getStatus().trim().toUpperCase();

        if ("PAID".equals(batchStatus)) {
            throw new IllegalStateException("Batch PAID không cho sửa.");
        }
        if ("REJECTED".equalsIgnoreCase(p.getSlipStatus())) {
            throw new IllegalStateException("Payslip REJECTED không thêm được.");
        }

        Benefit b = benefitService.requireActive(benefitId);

        BigDecimal base = nz(p.getBaseSalary());
        BigDecimal amount;
        if ("PERCENT_BASE".equalsIgnoreCase(b.getCalcMethod())) {
            amount = base.multiply(nz(b.getValue())).setScale(2, RoundingMode.HALF_UP);
        } else {
            amount = nz(b.getValue()).setScale(2, RoundingMode.HALF_UP);
        }

        String codeKey = (b.getCode() == null ? "" : b.getCode().trim().toUpperCase());
        if (codeKey.isBlank()) {
            throw new IllegalStateException("Benefit code is empty");
        }

        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslipId);

        PayslipItem it = items.stream()
                .filter(x -> x.getItemCode() != null && x.getItemCode().trim().equalsIgnoreCase(codeKey))
                .findFirst()
                .orElse(null);

        if (it == null) {
            it = PayslipItem.builder()
                    .payslip(p)
                    .itemCode(codeKey)
                    .itemName(b.getName())
                    .itemType(b.getType())
                    .amount(amount)
                    .manualAdjustment(true)
                    .build();
        } else {
            it.setItemName(b.getName());
            it.setItemType(b.getType());
            it.setAmount(amount);
            it.setManualAdjustment(true);
        }

        itemRepo.save(it);
        payrollCalculationService.recalcPayslipTotalsFromItems(p);
        payrollCalculationService.recalcBatchTotals(p.getBatch());
    }

    @Override
    public void approvePayslip(Integer managerEmpId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        validatePayslipAccess(p, managerEmpId, "Not allowed to approve this payslip");

        String batchStatus = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? "" : p.getBatch().getStatus().trim().toUpperCase();

        String slipStatus = (p.getSlipStatus() == null)
                ? "ACTIVE" : p.getSlipStatus().trim().toUpperCase();

        if ("PAID".equals(batchStatus) || "APPROVED".equals(batchStatus)) {
            throw new IllegalStateException("Batch đã chốt, không approve từng payslip nữa.");
        }

        if ("REJECTED".equals(slipStatus)) {
            throw new IllegalStateException("Payslip REJECTED không approve được.");
        }

        // approve lặp lại thì bỏ qua cho an toàn
        if ("APPROVED".equals(slipStatus)) {
            return;
        }

        // CHỈ approve 1 payslip, KHÔNG đụng vào batch
        p.setSlipStatus("APPROVED");
        p.setSentToEmployee(false); // chưa release cho employee ở bước approve từng dòng
        p.setRejectReason(null);
        p.setRejectedBy(null);
        p.setRejectedAt(null);

        payslipRepo.save(p);
    }

    @Override
    public SalaryUpdateResult updatePayslipBaseSalary(Integer managerEmpId,
                                                      Integer payslipId,
                                                      BigDecimal newBaseSalary) {

        if (newBaseSalary == null || newBaseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base salary invalid");
        }

        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        validatePayslipAccess(p, managerEmpId, "Not allowed to edit this payslip");

        String batchStatus = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? "" : p.getBatch().getStatus().trim().toUpperCase();

        if ("PAID".equals(batchStatus)) {
            throw new IllegalStateException("Batch PAID không cho sửa.");
        }

        boolean wasRejected = "REJECTED".equalsIgnoreCase(p.getSlipStatus());
        if (wasRejected) {
            p.setSlipStatus("ACTIVE");
            p.setSentToEmployee("APPROVED".equals(batchStatus));
        }

        LocalDate[] range = resolvePeriodRange(p);
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        BigDecimal base = newBaseSalary.setScale(2, RoundingMode.HALF_UP);

        PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                p.getEmployee() != null ? p.getEmployee().getId() : null,
                startDate,
                endDate,
                base,
                nz(p.getStandardWorkDays()),
                nz(p.getActualWorkDays()),
                nz(p.getOtHours())
        );

        p.setBaseSalary(base);
        p.setTotalIncome(c.totalIncome());
        p.setTotalDeduction(c.totalDeduction());
        p.setNetSalary(c.netSalary());
        payslipRepo.save(p);

        payrollCalculationService.upsertComputedItems(p, c, true);
        payrollCalculationService.recalcBatchTotals(p.getBatch());

        return new SalaryUpdateResult(p.getBaseSalary(), p.getNetSalary(), p.getSlipStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findBatchIdsByPayslipIds(List<Integer> payslipIds) {
        if (payslipIds == null || payslipIds.isEmpty()) {
            return List.of();
        }
        return payslipRepo.findBatchIdsByPayslipIds(payslipIds);
    }

    @Override
    public void rejectPayslip(Integer payslipId) {
        rejectPayslip(null, null, payslipId, "Rejected by manager");
    }

    @Override
    public void rejectPayslip(Integer accessEmpId, Integer rejectedByEmpId, Integer payslipId, String reason) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        validatePayslipAccess(p, accessEmpId, "Not allowed to reject this payslip");

        String batchStatus = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? ""
                : p.getBatch().getStatus().trim().toUpperCase();

        if (!"DRAFT".equals(batchStatus) && !"PENDING_APPROVAL".equals(batchStatus)) {
            throw new IllegalStateException("Chỉ reject được khi batch là DRAFT hoặc PENDING_APPROVAL.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do reject.");
        }

        p.setSlipStatus("REJECTED");
        p.setSentToEmployee(false);
        p.setRejectReason(reason.trim());
        p.setRejectedBy(rejectedByEmpId);
        p.setRejectedAt(LocalDateTime.now());
        payslipRepo.save(p);

        payrollCalculationService.recalcBatchTotals(p.getBatch());
        try {
            notifyHrAboutRejectedPayslip(p);
        } catch (Exception ex) {
            // Notification failure should not block the reject action
        }
    }

    private void notifyHrAboutRejectedPayslip(Payslip p) {
        List<Integer> hrEmpIds = userAccountRepo.findByRole_RoleNameIgnoreCaseAndActiveTrue("HR")
                .stream()
                .map(UserAccount::getId)
                .map(userId -> employeeRepo.findByUserId(userId).map(Employee::getId).orElse(null))
                .filter(Objects::nonNull)
                .filter(empId -> !Objects.equals(empId, p.getRejectedBy()))
                .distinct()
                .toList();
        if (hrEmpIds.isEmpty()) return;

        String employeeName = empName(p.getEmployee());
        String reason = p.getRejectReason() == null ? "" : p.getRejectReason();

        for (Integer hrEmpId : hrEmpIds) {
            notificationService.create(
                    hrEmpId,
                    "PAYROLL_REJECTED",
                    "Payslip rejected by manager",
                    employeeName + " - Reason: " + reason,
                    "/hr/payroll/rejected/" + p.getId()
            );
        }
    }

    @Override
    @Transactional
    public void deletePayslipFromBatch(Integer accessEmpId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        validatePayslipAccess(p, accessEmpId, "Not allowed to delete this payslip");

        PayrollBatch batch = p.getBatch();
        if (batch == null || batch.getStatus() == null) {
            throw new IllegalStateException("Không thể xóa phiếu lương không thuộc batch nào.");
        }

        String batchStatus = batch.getStatus().trim().toUpperCase();
        if (!"DRAFT".equals(batchStatus) && !"REJECTED".equals(batchStatus)) {
            throw new IllegalStateException("Chỉ được phép xóa phiếu lương khi bảng lương đang ở trạng thái DRAFT hoặc REJECTED.");
        }

        // Xóa inquiries phụ thuộc (nếu có) của payslip này
        inquiryRepo.deleteByPayslipId(payslipId);
        
        // Xóa các items phụ thuộc
        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslipId);
        itemRepo.deleteAll(items);

        Employee emp = p.getEmployee();
        if (emp != null) {
            emp.setIncludeInPayroll(false);
            employeeRepo.save(emp);
        }

        payslipRepo.delete(p);

        // Calculate and save new batch totals
        payrollCalculationService.recalcBatchTotals(batch);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PayrollRowDTO> listPayrollRowsForManager(Integer managerEmpId, String q, String status, Integer periodId) {
        Integer empId = null;
        if (q != null) {
            String t = q.trim().toUpperCase();
            t = t.replaceAll("\\s+", "");
            t = t.replaceAll("-", "");

            if (t.matches("^NV\\d+$")) {
                empId = Integer.parseInt(t.substring(2));
            } else if (t.matches("^\\d+$")) {
                empId = Integer.parseInt(t);
            }
        }

        List<Object[]> rows = payslipRepo.findPayrollRowsRaw(managerEmpId, status, q, empId, periodId);

        List<PayrollRowDTO> result = rows.stream().map(r -> {
            int payslipId = toInt(r[0]);
            int batchId = toInt(r[1]);
            int eId = toInt(r[2]);
            String fullName = (String) r[3];

            int month = toInt(r[4]);
            int year = toInt(r[5]);

            LocalDate start = toLocalDate(r[6]);
            LocalDate end = toLocalDate(r[7]);

            BigDecimal savedTotalIncome = toBigDecimal(r[8]);
            BigDecimal savedTotalDeduction = toBigDecimal(r[9]);
            BigDecimal savedNet = toBigDecimal(r[10]);

            String batchStatus = (String) r[11];
            String batchName = (String) r[12];

            BigDecimal baseSalary = toBigDecimal(r[13]);
            BigDecimal standardDays = toBigDecimal(r[14]);
            BigDecimal savedActualDays = toBigDecimal(r[15]);
            BigDecimal savedOtHours = toBigDecimal(r[16]);

            String jobTitle = (String) r[17];
            Boolean sentToEmployee = (r[18] instanceof Boolean b) ? b : Boolean.FALSE;
            String slipStatus = (String) r[19];
            String rejectReason = (String) r[20];
            LocalDateTime rejectedAt = null;
            if (r[21] instanceof java.sql.Timestamp ts) {
                rejectedAt = ts.toLocalDateTime();
            } else if (r[21] instanceof LocalDateTime ldt) {
                rejectedAt = ldt;
            }

            BigDecimal actualDays = savedActualDays;
            BigDecimal otHours = savedOtHours;
            BigDecimal totalIncome = savedTotalIncome;
            BigDecimal totalDeduction = savedTotalDeduction;
            BigDecimal net = savedNet;

            // Lấy attendance thật để đổ lên manager payroll list
            if (start != null && end != null && eId > 0) {
                long actualDaysLong = attendanceRepo.countActualWorkDays(eId, start, end);
                actualDays = BigDecimal.valueOf(actualDaysLong);

                long otMinutes = requestRepo.sumApprovedOvertimeMinutes(
                        eId,
                        start.atStartOfDay(),
                        end.atTime(LocalTime.MAX)
                );

                otHours = BigDecimal.valueOf(otMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                        eId,
                        start,
                        end,
                        baseSalary,
                        standardDays,
                        actualDays,
                        otHours
                );

                totalIncome = nz(c.totalIncome());
                totalDeduction = nz(c.totalDeduction());
                net = nz(c.netSalary());
            }

            BigDecimal dailySalary = BigDecimal.ZERO;
            if (standardDays.compareTo(BigDecimal.ZERO) > 0) {
                dailySalary = baseSalary.divide(standardDays, 2, RoundingMode.HALF_UP);
            }

            return PayrollRowDTO.builder()
                    .payslipId(payslipId)
                    .batchId(batchId)
                    .empId(eId)
                    .empCode("NV" + eId)
                    .fullName(fullName)
                    .month(month)
                    .year(year)
                    .startDate(start)
                    .endDate(end)
                    .jobTitle(jobTitle == null ? "" : jobTitle)
                    .batchName(batchName == null ? "" : batchName)
                    .baseSalary(baseSalary)
                    .standardWorkDays(standardDays)
                    .actualWorkDays(actualDays)
                    .otHours(otHours)
                    .dailySalary(dailySalary)
                    .totalIncome(totalIncome)
                    .totalDeduction(totalDeduction)
                    .netSalary(net)
                    .batchStatus(batchStatus)
                    .statusLabel(statusLabel(batchStatus))
                    .sentToEmployee(Boolean.TRUE.equals(sentToEmployee))
                    .slipStatus(slipStatus == null ? "ACTIVE" : slipStatus)
                    .bankMissing(false)
                    .rejectReason(rejectReason)
                    .rejectedAt(rejectedAt)
                    .build();
        }).toList();

        List<Integer> empIds = result.stream()
                .map(PayrollRowDTO::getEmpId)
                .distinct()
                .toList();

        Set<Integer> hasBank = bankAccountRepo.findByEmpIdInAndIsPrimaryTrue(empIds).stream()
                .map(BankAccount::getEmpId)
                .collect(Collectors.toSet());

        result.forEach(dto -> dto.setBankMissing(!hasBank.contains(dto.getEmpId())));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriodSummaryDTO> listPayrollPeriods() {
        return periodRepo.findAllByOrderByYearDescMonthDesc()
                .stream()
                .map(p -> PayrollPeriodSummaryDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .month(p.getMonth())
                        .year(p.getYear())
                        .status(p.getStatus())
                        .locked(Boolean.TRUE.equals(p.getLocked()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollBatchSummaryDTO> listBatchesByPeriod(Integer periodId) {
        PayrollPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        return batchRepo.findByPeriodOrderByIdDesc(period)
                .stream()
                .map(b -> PayrollBatchSummaryDTO.builder()
                        .id(b.getId())
                        .periodId(periodId)
                        .name(b.getName())
                        .status(b.getStatus())
                        .totalGross(nz(b.getTotalGross()))
                        .totalNet(nz(b.getTotalNet()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollBatchDetailDTO viewBatchDetail(Integer batchId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        List<PayslipSummaryDTO> slips = payslipRepo.findByBatch_IdOrderByIdAsc(batchId)
                .stream()
                .map(s -> PayslipSummaryDTO.builder()
                        .payslipId(s.getId())
                        .batchId(batchId)
                        .empId(s.getEmployee().getId())
                        .employeeName(empName(s.getEmployee()))
                        .totalIncome(nz(s.getTotalIncome()))
                        .totalDeduction(nz(s.getTotalDeduction()))
                        .netSalary(nz(s.getNetSalary()))
                        .build())
                .toList();

        return PayrollBatchDetailDTO.builder()
                .batchId(batch.getId())
                .periodId(batch.getPeriod().getId())
                .batchName(batch.getName())
                .status(batch.getStatus())
                .totalGross(nz(batch.getTotalGross()))
                .totalNet(nz(batch.getTotalNet()))
                .payslips(slips)
                .rejectReason(batch.getRejectReason())
                .rejectedBy(batch.getRejectedBy())
                .build();
    }

    @Override
    public Integer createPayrollPeriod(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ.");
        }

        if (year == null || year < 2000 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ.");
        }

        if (periodRepo.existsByMonthAndYear(month, year)) {
            throw new IllegalStateException("Kỳ lương này đã tồn tại.");
        }

        YearMonth ym = YearMonth.of(year, month);

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        PayrollPeriod period = PayrollPeriod.builder()
                .name(String.format("Payroll %02d/%d", month, year))
                .month(month)
                .year(year)
                .startDate(startDate)
                .endDate(endDate)
                .status("OPEN")
                .locked(false)
                .build();

        period = periodRepo.save(period);
        return period.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForManager(Integer managerEmpId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        validatePayslipAccess(p, managerEmpId, "Not allowed to view this payslip");

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
        if (emp != null && startDate != null) {
            long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), startDate, endDate);
            actualWorkDays = BigDecimal.valueOf(actualDaysLong);
        }

        return PayslipDetailDTO.builder()
                .payslipId(p.getId())
                .batchId(p.getBatch().getId())
                .empId(emp != null ? emp.getId() : null)
                .employeeName(empName(emp))
                .period(periodLabel(p.getBatch() != null ? p.getBatch().getPeriod() : null))
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
    public Integer generatePayrollDraft(Integer periodId, Integer createdByEmpId) {
        PayrollPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        if (Boolean.TRUE.equals(period.getLocked())) {
            throw new IllegalStateException("Period is locked.");
        }

        List<PayrollBatch> existingBatches = batchRepo.findByPeriodOrderByIdDesc(period);
        for (PayrollBatch existing : existingBatches) {
            String st = existing.getStatus() == null ? "" : existing.getStatus().trim().toUpperCase();
            if ("APPROVED".equals(st) || "PENDING_APPROVAL".equals(st)) {
                throw new IllegalStateException(
                        "Period này đã có batch \"" + existing.getName() + "\" trạng thái " + st +
                                ". Không thể tạo thêm. Nếu muốn làm lại, hãy xóa batch cũ trước.");
            }
        }

        for (PayrollBatch existing : existingBatches) {
            String st = existing.getStatus() == null ? "" : existing.getStatus().trim().toUpperCase();
            if ("DRAFT".equals(st) || "REJECTED".equals(st)) {
                inquiryRepo.deleteByBatchId(existing.getId());
                itemRepo.deleteByBatchId(existing.getId());
                payslipRepo.deleteByBatchId(existing.getId());
                batchRepo.delete(existing);
            }
        }

        LocalDate start = Optional.ofNullable(period.getStartDate())
                .orElse(LocalDate.of(period.getYear(), period.getMonth(), 1));

        LocalDate end = Optional.ofNullable(period.getEndDate())
                .orElse(start.withDayOfMonth(start.lengthOfMonth()));

        period.setStatus("CALCULATING");

        PayrollBatch batch = PayrollBatch.builder()
                .period(period)
                .name("Payroll " + period.getMonth() + "/" + period.getYear())
                .status("DRAFT")
                .createdBy(createdByEmpId)
                .totalGross(BigDecimal.ZERO)
                .totalNet(BigDecimal.ZERO)
                .build();
        batch = batchRepo.save(batch);

// CHỈ auto add những nhân viên đã được đưa vào payroll
        List<Employee> employees = employeeRepo.findPayrollEligibleEmployees(null);

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Employee emp : employees) {
            BigDecimal baseSalary = contractRepo.findActiveContractForPeriod(emp.getId(), start, end)
                    .map(Contract::getBaseSalary)
                    .orElse(BigDecimal.ZERO);

            BigDecimal standardDays = BigDecimal.valueOf(22);

            long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), start, end);
            BigDecimal actualDays = BigDecimal.valueOf(actualDaysLong);

            long otMinutes = requestRepo.sumApprovedOvertimeMinutes(
                    emp.getId(),
                    start.atStartOfDay(),
                    end.atTime(LocalTime.MAX)
            );

            BigDecimal otHours = BigDecimal.valueOf(otMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                    emp.getId(),
                    start,
                    end,
                    baseSalary,
                    standardDays,
                    actualDays,
                    otHours
            );

            Payslip slip = Payslip.builder()
                    .batch(batch)
                    .employee(emp)
                    .baseSalary(baseSalary)
                    .standardWorkDays(standardDays)
                    .actualWorkDays(actualDays)
                    .otHours(otHours)
                    .totalIncome(c.totalIncome())
                    .totalDeduction(c.totalDeduction())
                    .netSalary(c.netSalary())
                    .sentToEmployee(false)
                    .slipStatus("ACTIVE")
                    .build();

            slip = payslipRepo.save(slip);
            payrollCalculationService.upsertComputedItems(slip, c, false);

            totalGross = totalGross.add(c.totalIncome());
            totalNet = totalNet.add(c.netSalary());
        }

        batch.setTotalGross(totalGross);
        batch.setTotalNet(totalNet);
        batchRepo.save(batch);

        period.setStatus("OPEN");
        periodRepo.save(period);

        return batch.getId();
    }

    @Override
    public void submitBatchForApproval(Integer batchId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));

        if (!"DRAFT".equals(batch.getStatus())) {
            throw new IllegalStateException("Only DRAFT can be submitted.");
        }

        List<Payslip> slips = payslipRepo.findByBatch_IdOrderByIdAsc(batchId);
        boolean changed = false;
        for (Payslip slip : slips) {
            if ("APPROVED".equalsIgnoreCase(slip.getSlipStatus())) {
                slip.setSlipStatus("ACTIVE");
                changed = true;
            }
        }
        if (changed) {
            payslipRepo.saveAll(slips);
        }

        batch.setStatus("PENDING_APPROVAL");
        batchRepo.save(batch);
    }

    @Override
    public void approveBatch(Integer batchId, Integer approverEmpId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));

        String st = (batch.getStatus() == null ? "" : batch.getStatus().trim().toUpperCase());
        if (!"PENDING_APPROVAL".equals(st)) {
            throw new IllegalStateException("Only PENDING_APPROVAL can be approved.");
        }

        List<Payslip> slips = payslipRepo.findByBatch_IdOrderByIdAsc(batchId);
        slips.forEach(s -> {
            if (!"REJECTED".equalsIgnoreCase(s.getSlipStatus())) {
                s.setSentToEmployee(true);
            } else {
                s.setSentToEmployee(false);
            }
        });
        payslipRepo.saveAll(slips);

        batch.setStatus("APPROVED");
        batch.setApprovedBy(approverEmpId);
        batchRepo.save(batch);

        PayrollPeriod period = batch.getPeriod();
        period.setLocked(true);
        period.setStatus("CLOSED");
        periodRepo.save(period);
    }

    @Override
    public void unlockPeriod(Integer periodId) {
        PayrollPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));
        period.setLocked(false);
        period.setStatus("OPEN");
        periodRepo.save(period);
    }

    @Override
    public Integer deleteDraftBatch(Integer batchId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        String st = batch.getStatus() == null ? "" : batch.getStatus().trim().toUpperCase();
        if (!"DRAFT".equals(st) && !"REJECTED".equals(st)) {
            throw new IllegalStateException("Chỉ có thể xóa batch DRAFT hoặc REJECTED. Batch này đang ở trạng thái: " + st);
        }
        Integer periodId = batch.getPeriod() != null ? batch.getPeriod().getId() : null;
        inquiryRepo.deleteByBatchId(batchId);
        itemRepo.deleteByBatchId(batchId);
        payslipRepo.deleteByBatchId(batchId);
        batchRepo.delete(batch);
        return periodId;
    }

    @Override
    public void deletePeriod(Integer periodId) {
        PayrollPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));
        if (Boolean.TRUE.equals(period.getLocked())) {
            throw new IllegalStateException("Không thể xóa period đã bị khóa.");
        }
        if (!"OPEN".equalsIgnoreCase(period.getStatus())) {
            throw new IllegalStateException("Chỉ xóa được period ở trạng thái OPEN. Period này đang: " + period.getStatus());
        }
        // Xóa cascade: batches → payslip items → payslips → inquiries → batches → period
        List<PayrollBatch> batches = batchRepo.findByPeriodOrderByIdDesc(period);
        for (PayrollBatch batch : batches) {
            inquiryRepo.deleteByBatchId(batch.getId());
            itemRepo.deleteByBatchId(batch.getId());
            payslipRepo.deleteByBatchId(batch.getId());
            batchRepo.delete(batch);
        }
        periodRepo.delete(period);
    }

    @Override
    public void rejectBatch(Integer batchId, Integer rejectedByEmpId, String reason) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        String st = (batch.getStatus() == null ? "" : batch.getStatus().trim().toUpperCase());

        if (!"DRAFT".equals(st) && !"PENDING_APPROVAL".equals(st)) {
            throw new IllegalStateException("Reject chỉ áp dụng cho batch DRAFT hoặc PENDING_APPROVAL.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do reject.");
        }

        // Reset các payslip đang ở trạng thái APPROVED về ACTIVE để có thể sửa lại
        List<Payslip> slips = payslipRepo.findByBatch_IdOrderByIdAsc(batchId);
        boolean changed = false;
        for (Payslip slip : slips) {
            if ("APPROVED".equalsIgnoreCase(slip.getSlipStatus())) {
                slip.setSlipStatus("ACTIVE");
                changed = true;
            }
        }
        if (changed) {
            payslipRepo.saveAll(slips);
        }

        // Đặt status REJECTED, GIỮ LẠI toàn bộ dữ liệu payslips/items
        batch.setStatus("REJECTED");
        batch.setRejectReason(reason.trim());
        batch.setRejectedBy(rejectedByEmpId);
        batchRepo.save(batch);

        // Thông báo cho toàn bộ HR
        notifyHrAboutRejectedBatch(batch, reason.trim());
    }

    private void notifyHrAboutRejectedBatch(PayrollBatch batch, String reason) {
        String batchLabel = batch.getName() == null ? ("Batch #" + batch.getId()) : batch.getName();
        List<Integer> hrEmpIds = userAccountRepo.findByRole_RoleNameIgnoreCaseAndActiveTrue("HR")
                .stream()
                .map(UserAccount::getId)
                .map(userId -> employeeRepo.findByUserId(userId).map(Employee::getId).orElse(null))
                .filter(Objects::nonNull)
                .filter(empId -> !Objects.equals(empId, batch.getRejectedBy()))
                .distinct()
                .toList();

        for (Integer hrEmpId : hrEmpIds) {
            notificationService.create(
                    hrEmpId,
                    "PAYROLL_BATCH_REJECTED",
                    "Batch lương bị reject",
                    batchLabel + " đã bị reject - Lý do: " + reason,
                    "/manager/payroll/batches/" + batch.getId()
            );
        }
    }

    private void validatePayslipAccess(Payslip p, Integer managerEmpId, String message) {
        if (managerEmpId == null)
            return;

        Integer ownerEmpId = p.getEmployee() != null ? p.getEmployee().getId() : null;
        Integer direct = p.getEmployee() != null ? p.getEmployee().getDirectManagerId() : null;

        boolean ok = Objects.equals(ownerEmpId, managerEmpId)
                || Objects.equals(direct, managerEmpId);

        if (!ok) {
            throw new AccessDeniedException(message);
        }
    }

    private LocalDate[] resolvePeriodRange(Payslip p) {
        PayrollPeriod per = (p.getBatch() != null) ? p.getBatch().getPeriod() : null;

        if (per != null) {
            LocalDate s = per.getStartDate();
            LocalDate e = per.getEndDate();

            if (s == null) {
                int m = (per.getMonth() != null ? per.getMonth() : LocalDate.now().getMonthValue());
                int y = (per.getYear() != null ? per.getYear() : LocalDate.now().getYear());
                s = LocalDate.of(y, m, 1);
            }
            if (e == null) {
                e = s.withDayOfMonth(s.lengthOfMonth());
            }
            return new LocalDate[]{s, e};
        }

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return new LocalDate[]{start, end};
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null)
            return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd)
            return bd;
        if (v instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(v.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private int toInt(Object v) {
        if (v == null)
            return 0;
        if (v instanceof Number n)
            return n.intValue();
        return Integer.parseInt(v.toString());
    }

    private LocalDate toLocalDate(Object v) {
        if (v == null)
            return null;
        if (v instanceof LocalDate ld)
            return ld;
        if (v instanceof java.sql.Date d)
            return d.toLocalDate();
        if (v instanceof java.sql.Timestamp ts)
            return ts.toLocalDateTime().toLocalDate();
        if (v instanceof java.util.Date ud)
            return new java.sql.Date(ud.getTime()).toLocalDate();
        return LocalDate.parse(v.toString());
    }

    private String statusLabel(String batchStatus) {
        if (batchStatus == null)
            return "";
        return switch (batchStatus) {
            case "DRAFT" -> "Draft";
            case "PENDING_APPROVAL" -> "Pending";
            case "APPROVED" -> "Approved";
            case "PAID" -> "Completed";
            default -> batchStatus;
        };
    }

    private String empName(Employee e) {
        if (e == null)
            return "";
        String n = e.getFullName();
        if (n != null && !n.trim().isEmpty())
            return n.trim();
        return "NV" + e.getId();
    }

    private String periodLabel(PayrollPeriod per) {
        if (per == null)
            return "";

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

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSearchResultDTO> searchEmployeesForBatch(Integer managerEmpId, Integer batchId, String keyword) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        if (!"DRAFT".equalsIgnoreCase(batch.getStatus())) {
            throw new IllegalStateException("Chỉ search employee cho batch DRAFT.");
        }

        String kw = keyword == null ? "" : keyword.trim();

        return employeeRepo.searchAvailableForBatch(
                        batchId,
                        managerEmpId,
                        kw,
                        org.springframework.data.domain.PageRequest.of(0, 20)
                )
                .stream()
                .map(e -> EmployeeSearchResultDTO.builder()
                        .empId(e.getId())
                        .empCode("NV" + e.getId())
                        .fullName(empName(e))
                        .displayText("NV" + e.getId() + " - " + empName(e))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollBatchSummaryDTO> listDraftBatches() {
        // Lấy tất cả batch DRAFT, sắp xếp theo kỳ mới nhất (year DESC, month DESC)
        List<PayrollBatch> allDrafts = batchRepo.findByStatusInOrderByIdDesc(List.of("DRAFT", "PENDING_APPROVAL"));

        // Tìm kỳ có year+month cao nhất trong danh sách
        OptionalInt maxYear = allDrafts.stream()
                .filter(b -> b.getPeriod() != null && b.getPeriod().getYear() != null)
                .mapToInt(b -> b.getPeriod().getYear())
                .max();

        if (maxYear.isEmpty()) {
            return List.of();
        }

        int latestYear = maxYear.getAsInt();
        OptionalInt maxMonth = allDrafts.stream()
                .filter(b -> b.getPeriod() != null
                        && b.getPeriod().getYear() != null
                        && b.getPeriod().getYear() == latestYear
                        && b.getPeriod().getMonth() != null)
                .mapToInt(b -> b.getPeriod().getMonth())
                .max();

        if (maxMonth.isEmpty()) {
            return List.of();
        }

        int latestMonth = maxMonth.getAsInt();

        // Chỉ trả về batch của kỳ mới nhất
        return allDrafts.stream()
                .filter(b -> b.getPeriod() != null
                        && b.getPeriod().getYear() != null
                        && b.getPeriod().getMonth() != null
                        && b.getPeriod().getYear() == latestYear
                        && b.getPeriod().getMonth() == latestMonth)
                .map(b -> PayrollBatchSummaryDTO.builder()
                        .id(b.getId())
                        .periodId(b.getPeriod().getId())
                        .name(b.getName())
                        .status(b.getStatus())
                        .totalGross(nz(b.getTotalGross()))
                        .totalNet(nz(b.getTotalNet()))
                        .build())
                .toList();
    }

    @Override
    public Integer addEmployeeToPayroll(Integer managerEmpId, Integer batchId, Integer empId, BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base salary invalid");
        }

        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        if (!"DRAFT".equalsIgnoreCase(batch.getStatus())) {
            throw new IllegalStateException("Chỉ thêm nhân viên vào batch DRAFT.");
        }

        Employee emp = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));

        String empStatus = emp.getStatus() == null ? "" : emp.getStatus().trim().toUpperCase();
        if (!"PROBATION".equals(empStatus) && !"OFFICIAL".equals(empStatus)) {
            throw new IllegalStateException("Chỉ nhân viên PROBATION hoặc OFFICIAL mới được thêm vào payroll.");
        }

        // Nếu employee chưa có manager thì tự gán cho manager đang add
        if (managerEmpId != null) {
            Integer directManagerId = emp.getDirectManagerId();

            if (directManagerId != null && !Objects.equals(directManagerId, managerEmpId)) {
                throw new AccessDeniedException("Nhân viên này không thuộc quyền quản lý của bạn.");
            }

            if (directManagerId == null) {
                emp.setDirectManagerId(managerEmpId);
            }
        }

        if (payslipRepo.existsByBatch_IdAndEmployee_Id(batchId, empId)) {
            throw new IllegalStateException("Employee này đã có payslip trong batch.");
        }

        emp.setIncludeInPayroll(true);
        employeeRepo.save(emp);

        PayrollPeriod period = batch.getPeriod();
        if (period == null) {
            throw new IllegalStateException("Batch chưa có payroll period.");
        }

        LocalDate start = Optional.ofNullable(period.getStartDate())
                .orElse(LocalDate.of(period.getYear(), period.getMonth(), 1));

        LocalDate end = Optional.ofNullable(period.getEndDate())
                .orElse(start.withDayOfMonth(start.lengthOfMonth()));

        BigDecimal standardDays = BigDecimal.valueOf(22);

        long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), start, end);
        BigDecimal actualDays = BigDecimal.valueOf(actualDaysLong);

        long otMinutes = requestRepo.sumApprovedOvertimeMinutes(
                emp.getId(),
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX)
        );

        BigDecimal otHours = BigDecimal.valueOf(otMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        PayrollCalculationService.PayslipComputation c = payrollCalculationService.compute(
                emp.getId(),
                start,
                end,
                baseSalary,
                standardDays,
                actualDays,
                otHours
        );

        Payslip slip = Payslip.builder()
                .batch(batch)
                .employee(emp)
                .baseSalary(baseSalary.setScale(2, RoundingMode.HALF_UP))
                .standardWorkDays(standardDays)
                .actualWorkDays(actualDays)
                .otHours(otHours)
                .totalIncome(c.totalIncome())
                .totalDeduction(c.totalDeduction())
                .netSalary(c.netSalary())
                .sentToEmployee(false)
                .slipStatus("ACTIVE")
                .build();

        slip = payslipRepo.save(slip);

        payrollCalculationService.upsertComputedItems(slip, c, false);
        payrollCalculationService.recalcBatchTotals(batch);

        return slip.getId();
    }

}