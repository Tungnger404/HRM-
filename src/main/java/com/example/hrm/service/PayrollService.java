package com.example.hrm.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

import org.springframework.core.io.ClassPathResource;

import com.example.hrm.dto.PayrollRowDTO;
import com.example.hrm.dto.*;
import com.example.hrm.repository.BankAccountRepository;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final PayrollPeriodRepository periodRepo;
    private final PayrollBatchRepository batchRepo;
    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final PayrollInquiryRepository inquiryRepo;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepo;
    private final ContractRepository contractRepo;
    private final AttendanceLogRepository attendanceRepo;
    private final RequestRepository requestRepo;

    // =========================
    // MANAGER SIDE
    // =========================
    private final BankAccountRepository bankAccountRepo;

    private static String ascii(String s) {
        if (s == null)
            return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}+", ""); // bỏ dấu
        n = n.replace("đ", "d").replace("Đ", "D");
        return n;
    }

    private static float writeLine(PDPageContentStream cs, PDFont font, int size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
        return y - 16;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String money(BigDecimal v) {
        BigDecimal val = (v == null ? BigDecimal.ZERO : v);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        nf.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return nf.format(val);
    }

    @Transactional(readOnly = true)
    public List<Integer> findBatchIdsByPayslipIds(List<Integer> payslipIds) {
        if (payslipIds == null || payslipIds.isEmpty())
            return List.of();
        return payslipRepo.findBatchIdsByPayslipIds(payslipIds);
    }

    @Transactional
    public void rejectPayslip(Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        // Chỉ cho reject khi batch DRAFT hoặc PENDING_APPROVAL (đúng flow)
        String st = (p.getBatch() == null || p.getBatch().getStatus() == null)
                ? ""
                : p.getBatch().getStatus().trim().toUpperCase();

        if (!"DRAFT".equals(st) && !"PENDING_APPROVAL".equals(st)) {
            throw new IllegalStateException("Chỉ reject được khi batch là DRAFT hoặc PENDING_APPROVAL.");
        }

        PayrollBatch b = p.getBatch();
        Integer batchId = (b != null ? b.getId() : null);

        // xóa con trước
        inquiryRepo.deleteByPayslipId(payslipId);
        itemRepo.deleteByPayslipId(payslipId);

        // update total trước khi xóa (tùy bạn có cần chuẩn số không)
        if (b != null) {
            BigDecimal gross = b.getTotalGross() == null ? BigDecimal.ZERO : b.getTotalGross();
            BigDecimal net = b.getTotalNet() == null ? BigDecimal.ZERO : b.getTotalNet();

            BigDecimal slipIncome = p.getTotalIncome() == null ? BigDecimal.ZERO : p.getTotalIncome();
            BigDecimal slipNet = p.getNetSalary() == null ? BigDecimal.ZERO : p.getNetSalary();

            b.setTotalGross(gross.subtract(slipIncome).max(BigDecimal.ZERO));
            b.setTotalNet(net.subtract(slipNet).max(BigDecimal.ZERO));
            batchRepo.save(b);
        }

        payslipRepo.deleteById(payslipId);

        // nếu batch rỗng thì xoá batch luôn (để sạch DB)
        if (batchId != null && payslipRepo.countByBatch_Id(batchId) == 0) {
            batchRepo.deleteById(batchId);
        }
    }

    @Transactional(readOnly = true)
    public List<PayrollRowDTO> listPayrollRowsForManager(Integer managerEmpId, String q, String status) {

        Integer empId = null;
        if (q != null) {
            String t = q.trim().toUpperCase();

            // ✅ hỗ trợ NV1, NV-1, NV 1
            t = t.replaceAll("\\s+", "");     // remove spaces: "NV 1" -> "NV1"
            t = t.replaceAll("-", "");        // remove hyphen: "NV-1" -> "NV1"

            if (t.matches("^NV\\d+$")) {
                empId = Integer.parseInt(t.substring(2));
            } else if (t.matches("^\\d+$")) {
                empId = Integer.parseInt(t);
            }
        }

        List<Object[]> rows = payslipRepo.findPayrollRowsRaw(managerEmpId, status, q, empId);

        List<PayrollRowDTO> result = rows.stream().map(r -> {
            int payslipId = toInt(r[0]);
            int batchId = toInt(r[1]);
            int eId = toInt(r[2]);
            String fullName = (String) r[3];

            int month = toInt(r[4]);
            int year = toInt(r[5]);

            LocalDate start = toLocalDate(r[6]);
            LocalDate end = toLocalDate(r[7]);

            BigDecimal totalIncome = toBigDecimal(r[8]);
            BigDecimal totalDeduction = toBigDecimal(r[9]);
            BigDecimal net = toBigDecimal(r[10]);

            String batchStatus = (String) r[11];
            String batchName = (String) r[12];

            BigDecimal baseSalary = toBigDecimal(r[13]);
            BigDecimal standardDays = toBigDecimal(r[14]);
            BigDecimal actualDays = toBigDecimal(r[15]);
            BigDecimal otHours = toBigDecimal(r[16]);

            String jobTitle = (String) r[17];
            Boolean sentToEmployee = (r[18] instanceof Boolean b) ? b : Boolean.FALSE;

            BigDecimal dailySalary = BigDecimal.ZERO;
            if (standardDays != null && standardDays.compareTo(BigDecimal.ZERO) > 0) {
                dailySalary = baseSalary.divide(standardDays, 2, RoundingMode.HALF_UP);
            }

            String empCode = "NV" + eId;
            String label = statusLabel(batchStatus);

            return PayrollRowDTO.builder()
                    .payslipId(payslipId)
                    .batchId(batchId)
                    .empId(eId)
                    .empCode(empCode)
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
                    .statusLabel(label)

                    .sentToEmployee(Boolean.TRUE.equals(sentToEmployee))
                    .bankMissing(false) // set sau
                    .build();
        }).toList();

        // ===== CHECK BANK ACCOUNT PRIMARY (enterprise check) =====
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

    private BigDecimal toBd(Object v) {
        if (v == null)
            return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd)
            return bd;
        return new BigDecimal(v.toString());
    }

    /**
     * ✅ helper: convert Object -> BigDecimal safe
     */
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
            return n.intValue(); // Integer/Long/BigInteger...
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
        // fallback
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
                        .employeeName(empName(s.getEmployee())) // ✅ FIX
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
                .build();
    }

    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForManager(Integer managerEmpId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        // ✅ quyền: manager xem được payslip của chính mình hoặc nhân viên thuộc quyền
        if (managerEmpId != null) {
            Integer ownerEmpId = p.getEmployee() != null ? p.getEmployee().getId() : null;
            Integer direct = p.getEmployee() != null ? p.getEmployee().getDirectManagerId() : null;

            boolean ok = Objects.equals(ownerEmpId, managerEmpId)
                    || Objects.equals(direct, managerEmpId);

            if (!ok) {
                throw new AccessDeniedException("Not allowed to view this payslip");
            }
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

    /**
     * Generate Payroll Draft (System logic) - bạn có thể gọi từ Manager button
     * - Lấy base_salary từ contract ACTIVE
     * - actual_work_days từ attendance_logs
     * - ot_hours từ requests (OVERTIME APPROVED)
     * - tạo payslip + payslip_items
     */
    @Transactional
    public Integer generatePayrollDraft(Integer periodId, Integer createdByEmpId) {
        PayrollPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        if (Boolean.TRUE.equals(period.getLocked())) {
            throw new IllegalStateException("Period is locked.");
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

        // Lấy nhân viên active để tính lương
        List<Employee> employees = employeeRepo.findAll()
                .stream()
                .filter(e -> e.getStatus() == null || !List.of("RESIGNED", "TERMINATED").contains(e.getStatus()))
                .toList();

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Employee emp : employees) {

            BigDecimal baseSalary = contractRepo.findActiveContractForPeriod(emp.getId(), start, end)
                    .map(Contract::getBaseSalary)
                    .orElse(BigDecimal.ZERO);

            BigDecimal standardDays = BigDecimal.valueOf(22); // demo rule
            long actualDaysLong = attendanceRepo.countActualWorkDays(emp.getId(), start, end);
            BigDecimal actualDays = BigDecimal.valueOf(actualDaysLong);

            long otMinutes = requestRepo.sumApprovedOvertimeMinutes(
                    emp.getId(),
                    start.atStartOfDay(),
                    end.atTime(LocalTime.MAX));
            BigDecimal otHours = BigDecimal.valueOf(otMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            // salary = base * actual/standard
            BigDecimal salaryByDays = (standardDays.compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : baseSalary.multiply(actualDays).divide(standardDays, 2, RoundingMode.HALF_UP);

            // overtime pay = (base/standard/8) * otHours * 1.5
            BigDecimal hourly = (standardDays.compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : baseSalary.divide(standardDays, 2, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);

            BigDecimal overtimePay = hourly.multiply(otHours).multiply(BigDecimal.valueOf(1.5))
                    .setScale(2, RoundingMode.HALF_UP);

            // ===== BENEFITS (Phúc lợi) =====
            // bạn đổi số tiền/ngày ở đây
            // ===== BENEFITS (cố định mỗi tháng) =====
            BigDecimal mealAllowance = new BigDecimal("50000");       // 50,000 / tháng
            BigDecimal transportAllowance = new BigDecimal("100000"); // 100,000 / tháng

            BigDecimal totalIncome = salaryByDays
                    .add(overtimePay)
                    .add(mealAllowance)
                    .add(transportAllowance);

            // ===== DEDUCTION: BHYT/BHXH 3% (demo) =====
            BigDecimal insuranceRate = new BigDecimal("0.03"); // 3%
            BigDecimal insuranceDeduction = baseSalary.multiply(insuranceRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalDeduction = insuranceDeduction;
            BigDecimal net = totalIncome.subtract(totalDeduction);

            Payslip slip = Payslip.builder()
                    .batch(batch)
                    .employee(emp)
                    .baseSalary(baseSalary)
                    .standardWorkDays(standardDays)
                    .actualWorkDays(actualDays)
                    .otHours(otHours)
                    .totalIncome(totalIncome)
                    .totalDeduction(totalDeduction)
                    .netSalary(net)
                    .sentToEmployee(false)
                    .build();
            slip = payslipRepo.save(slip);

            // items
            List<PayslipItem> items = new ArrayList<>();
            items.add(PayslipItem.builder()
                    .payslip(slip)
                    .itemCode("BASE_BY_DAYS")
                    .itemName("Salary by work days")
                    .amount(salaryByDays)
                    .itemType("INCOME")
                    .manualAdjustment(false)
                    .build());

            if (overtimePay.compareTo(BigDecimal.ZERO) > 0) {
                items.add(PayslipItem.builder()
                        .payslip(slip)
                        .itemCode("OT_PAY")
                        .itemName("Overtime pay")
                        .amount(overtimePay)
                        .itemType("INCOME")
                        .manualAdjustment(false)
                        .build());
            }

            if (mealAllowance.compareTo(BigDecimal.ZERO) > 0) {
                items.add(PayslipItem.builder()
                        .payslip(slip)
                        .itemCode("MEAL_ALLOW")
                        .itemName("Meal allowance")
                        .amount(mealAllowance)
                        .itemType("INCOME")
                        .manualAdjustment(false)
                        .build());
            }

            if (transportAllowance.compareTo(BigDecimal.ZERO) > 0) {
                items.add(PayslipItem.builder()
                        .payslip(slip)
                        .itemCode("TRANSPORT_ALLOW")
                        .itemName("Transport allowance")
                        .amount(transportAllowance)
                        .itemType("INCOME")
                        .manualAdjustment(false)
                        .build());
            }

            if (insuranceDeduction.compareTo(BigDecimal.ZERO) > 0) {
                items.add(PayslipItem.builder()
                        .payslip(slip)
                        .itemCode("BHYT")          // bạn đổi thành "BHXH" nếu muốn
                        .itemName("BHYT (3%)")     // hiển thị
                        .amount(insuranceDeduction)
                        .itemType("DEDUCTION")
                        .manualAdjustment(false)
                        .build());
            }

            itemRepo.saveAll(items);

            totalGross = totalGross.add(totalIncome);
            totalNet = totalNet.add(net);
        }

        batch.setTotalGross(totalGross);
        batch.setTotalNet(totalNet);
        batchRepo.save(batch);

        period.setStatus("OPEN"); // quay về OPEN, chờ manager review
        periodRepo.save(period);

        return batch.getId();
    }

    // =========================
    // EMPLOYEE SIDE
    // =========================

    @Transactional
    public void submitBatchForApproval(Integer batchId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (!"DRAFT".equals(batch.getStatus())) {
            throw new IllegalStateException("Only DRAFT can be submitted.");
        }
        batch.setStatus("PENDING_APPROVAL");
        batchRepo.save(batch);
    }

    @Transactional
    public void approveBatch(Integer batchId, Integer approverEmpId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (!"PENDING_APPROVAL".equals(batch.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL can be approved.");
        }
        batch.setStatus("APPROVED");
        batch.setApprovedBy(approverEmpId);
        batchRepo.save(batch);

        // lock period
        PayrollPeriod period = batch.getPeriod();
        period.setLocked(true);
        period.setStatus("CLOSED");
        periodRepo.save(period);

        // mark payslips sent
        payslipRepo.findByBatch_IdOrderByIdAsc(batchId).forEach(s -> s.setSentToEmployee(true));
    }

    @Transactional
    public void rejectBatch(Integer batchId) {
        PayrollBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        String st = (batch.getStatus() == null ? "" : batch.getStatus().trim().toUpperCase());

        // ✅ Bạn muốn "Reject là biến mất luôn" => xóa batch cho cả DRAFT & PENDING_APPROVAL
        if ("DRAFT".equals(st) || "PENDING_APPROVAL".equals(st)) {

            // xóa theo thứ tự để không vướng FK
            inquiryRepo.deleteByBatchId(batchId);
            itemRepo.deleteByBatchId(batchId);
            payslipRepo.deleteByBatchId(batchId);
            batchRepo.delete(batch);

            return;
        }

        throw new IllegalStateException("Reject chỉ áp dụng cho batch DRAFT hoặc PENDING_APPROVAL.");
    }

    // Export Excel payroll batch
    @Transactional(readOnly = true)
    public byte[] exportBatchExcel(Integer batchId) {
        PayrollBatchDetailDTO dto = viewBatchDetail(batchId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Payroll");

            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("PayslipId");
            h.createCell(1).setCellValue("EmpId");
            h.createCell(2).setCellValue("Employee");
            h.createCell(3).setCellValue("TotalIncome");
            h.createCell(4).setCellValue("TotalDeduction");
            h.createCell(5).setCellValue("NetSalary");

            int r = 1;
            for (PayslipSummaryDTO s : dto.getPayslips()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getPayslipId());
                row.createCell(1).setCellValue(s.getEmpId());
                row.createCell(2).setCellValue(s.getEmployeeName());
                row.createCell(3).setCellValue(nz(s.getTotalIncome()).doubleValue());
                row.createCell(4).setCellValue(nz(s.getTotalDeduction()).doubleValue());
                row.createCell(5).setCellValue(nz(s.getNetSalary()).doubleValue());
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Export Excel failed", e);
        }
    }

    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listAllInquiriesForEmployee(Integer empId, String status) {
        // dùng repo query riêng cho employee
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

    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listInquiriesForEmployee(Integer empId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        // chỉ chủ payslip mới xem được
        if (!p.getEmployee().getId().equals(empId)) {
            throw new SecurityException("Not allowed");
        }

        // chỉ xem khi đã release
        if (!Boolean.TRUE.equals(p.getSentToEmployee())) {
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


    // PDF download for payslip

    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetailForEmployee(Integer empId, Integer payslipId) {
        Payslip p = payslipRepo.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        // chỉ chủ payslip mới xem được
        if (!p.getEmployee().getId().equals(empId)) {
            throw new SecurityException("Not allowed");
        }

        // chỉ xem khi đã release
        if (!Boolean.TRUE.equals(p.getSentToEmployee())) {
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
                .period(periodLabel(p.getBatch() != null ? p.getBatch().getPeriod() : null)) // ✅ ADD
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

    @Transactional(readOnly = true)
    public List<PayslipSummaryDTO> listEmployeePayslips(Integer empId) {
        Employee emp = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));

        return payslipRepo.findByEmployeeOrderByIdDesc(emp)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getSentToEmployee()))
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

    @Transactional
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
        // ===== NOTIFY MANAGER when employee submitted inquiry =====
        notificationService.createPayrollInquirySubmitted(
                emp.getDirectManagerId(),   // manager emp_id
                inq.getId(),                // inquiry_id
                p.getId(),                  // payslip_id
                empName(emp)                // employee name
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

    @Transactional(readOnly = true)
    public byte[] exportBankTransferExcel(List<Integer> batchIds) {

        // Lấy payslips thuộc các batch
        List<Payslip> slips = batchIds.stream()
                .flatMap(id -> payslipRepo.findByBatch_IdOrderByIdAsc(id).stream())
                .toList();

        // group theo emp
        Map<Integer, List<Payslip>> byEmp = slips.stream()
                .collect(Collectors.groupingBy(s -> s.getEmployee().getId()));

        List<Integer> empIds = byEmp.keySet().stream().toList();

        // lấy bank account primary (nếu thiếu sẽ để trống)
        Map<Integer, BankAccount> bankMap = bankAccountRepo.findByEmpIdInAndIsPrimaryTrue(empIds)
                .stream()
                .collect(Collectors.toMap(BankAccount::getEmpId, x -> x, (a, b) -> a));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("SalaryTransfer");

            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("EmpId");
            h.createCell(1).setCellValue("Employee");
            h.createCell(2).setCellValue("NetTotal");
            h.createCell(3).setCellValue("BankName");
            h.createCell(4).setCellValue("AccountNumber");
            h.createCell(5).setCellValue("HolderName");
            h.createCell(6).setCellValue("Note");

            int r = 1;
            for (var entry : byEmp.entrySet()) {
                Integer empId = entry.getKey();
                List<Payslip> list = entry.getValue();

                BigDecimal net = list.stream()
                        .map(Payslip::getNetSalary)
                        .map(v -> v == null ? BigDecimal.ZERO : v)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String empName = empName(list.get(0).getEmployee());
                BankAccount ba = bankMap.get(empId);

                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(empId);
                row.createCell(1).setCellValue(empName);
                row.createCell(2).setCellValue(net.doubleValue());
                row.createCell(3).setCellValue(ba != null ? ba.getBankName() : "");
                row.createCell(4).setCellValue(ba != null ? ba.getAccountNumber() : "");
                row.createCell(5).setCellValue(ba != null ? ba.getAccountHolderName() : "");
                row.createCell(6).setCellValue(ba == null ? "MISSING_BANK_ACCOUNT" : "");
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Export bank transfer excel failed", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] buildPayslipPdf(Integer empId, Integer payslipId) {
        PayslipDetailDTO dto = getPayslipDetailForEmployee(empId, payslipId);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // ✅ check font có tồn tại trong classpath không
            System.out.println("FONT arial.ttf exists? " +
                    new ClassPathResource("static/fonts/arial.ttf").exists());
            System.out.println("FONT arialbd.ttf exists? " +
                    new ClassPathResource("static/fonts/arialbd.ttf").exists());

            // ✅ load font
            PDType0Font font = tryLoadTtf(doc, "static/fonts/arial.ttf");
            PDType0Font fontBold = tryLoadTtf(doc, "static/fonts/arialbd.ttf");
            final boolean unicodeOk = (font != null && fontBold != null);

            final PDFont f = unicodeOk ? font : PDType1Font.HELVETICA;
            final PDFont fb = unicodeOk ? fontBold : PDType1Font.HELVETICA_BOLD;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float x = 50;
                float y = 770;

                String title = unicodeOk ? "PHIẾU LƯƠNG / PAYSLIP" : "PAYSLIP";
                y = writeLine(cs, fb, 16, x, y, unicodeOk ? title : ascii(title));
                y -= 8;

                String empName = safe(dto.getEmployeeName());
                String period = safe(dto.getPeriod());

                y = writeLine(cs, f, 11, x, y, (unicodeOk ? "Nhân viên: " : "Employee: ")
                        + (unicodeOk ? empName : ascii(empName)));

                y = writeLine(cs, f, 11, x, y, (unicodeOk ? "Kỳ lương: " : "Period: ")
                        + (unicodeOk ? period : ascii(period)));

                y = writeLine(cs, f, 11, x, y, "Payslip ID: " + dto.getPayslipId());
                y = writeLine(cs, f, 11, x, y, (unicodeOk ? "Tổng thu nhập: " : "Total income: ") + money(dto.getTotalIncome()));
                y = writeLine(cs, f, 11, x, y, (unicodeOk ? "Khấu trừ: " : "Deduction: ") + money(dto.getTotalDeduction()));
                y = writeLine(cs, fb, 12, x, y, (unicodeOk ? "Thực nhận (Net): " : "Net: ") + money(dto.getNetSalary()));

                y -= 10;
                y = writeLine(cs, fb, 12, x, y, unicodeOk ? "Chi tiết khoản mục:" : "Items:");

                List<PayslipItemDTO> items = Optional.ofNullable(dto.getItems()).orElse(List.of());
                if (items.isEmpty()) {
                    y = writeLine(cs, f, 11, x, y, unicodeOk ? "- (Không có khoản mục)" : "- (No items)");
                } else {
                    for (PayslipItemDTO it : items) {
                        String line = String.format("- [%s] %s: %s",
                                safe(it.getType()),
                                safe(it.getName()),
                                money(it.getAmount())
                        );
                        y = writeLine(cs, f, 11, x, y, unicodeOk ? line : ascii(line));
                        if (y < 60)
                            break;
                    }
                }
            }

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Build PDF failed", e);
        }
    }

    private PDType0Font tryLoadTtf(PDDocument doc, String classpathLocation) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return PDType0Font.load(doc, in, true);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    // === MANAGER - INQUIRIES ===

    @Transactional(readOnly = true)
    public List<PayrollInquiryDTO> listInquiriesForManager(Integer managerEmpId, String status) {
        return inquiryRepo.findForManager(managerEmpId, status).stream()
                .map(i -> PayrollInquiryDTO.builder()
                        .id(i.getId())
                        .payslipId(i.getPayslip().getId())
                        .empId(i.getEmployee().getId())
                        .employeeName(empName(i.getEmployee())) // ✅ NEW
                        .question(i.getQuestion())
                        .answer(i.getAnswer())
                        .status(i.getStatus())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollInquiryDTO getInquiry(Integer inquiryId) {
        var i = inquiryRepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        return PayrollInquiryDTO.builder()
                .id(i.getId())
                .payslipId(i.getPayslip().getId())
                .empId(i.getEmployee().getId())
                .employeeName(empName(i.getEmployee())) // ✅ NEW
                .question(i.getQuestion())
                .answer(i.getAnswer())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .build();
    }

    @Transactional
    public void resolveInquiry(Integer managerEmpId, Integer inquiryId, String answer) {
        var i = inquiryRepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        // quyền: chỉ manager trực tiếp mới resolve
        Integer direct = i.getEmployee().getDirectManagerId();
        if (direct == null || !direct.equals(managerEmpId)) {
            throw new SecurityException("Not allowed to resolve this inquiry");
        }

        i.setAnswer(answer);
        i.setStatus("RESOLVED");
        inquiryRepo.save(i);
        // ===== NOTIFY EMPLOYEE when manager resolved inquiry =====
        notificationService.createPayrollInquiryResolved(
                i.getEmployee().getId(),    // employee emp_id
                i.getId(),                  // inquiry_id
                i.getPayslip().getId()      // payslip_id
        );
    }

    private String empName(Employee e) {
        if (e == null)
            return "";
        String n = e.getFullName();
        if (n != null && !n.trim().isEmpty())
            return n.trim();
        return "NV" + e.getId(); // fallback
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

}
