package com.example.hrm.service;

import com.example.hrm.dto.*;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

    private final PayrollPeriodRepository periodRepo;
    private final PayrollBatchRepository batchRepo;
    private final PayslipRepository payslipRepo;
    private final PayslipItemRepository itemRepo;
    private final PayrollInquiryRepository inquiryRepo;

    private final EmployeeRepository employeeRepo;
    private final ContractRepository contractRepo;
    private final AttendanceLogRepository attendanceRepo;
    private final RequestRepository requestRepo;

    // =========================
    // MANAGER SIDE
    // =========================

    @Transactional(readOnly = true)
    public List<PayrollRowDTO> listPayrollRowsForManager(Integer managerEmpId, String q, String status) {

        Integer empId = null;
        if (q != null) {
            String t = q.trim();
            if (t.matches("\\d+")) {
                try {
                    empId = Integer.parseInt(t);
                } catch (Exception ignored) {
                }
            }
        }

        List<Object[]> rows = payslipRepo.findPayrollRowsRaw(managerEmpId, status, q, empId);

        return rows.stream().map(r -> {
            int payslipId = toInt(r[0]);
            int batchId = toInt(r[1]);
            int eId = toInt(r[2]);
            String fullName = (String) r[3];

            int month = toInt(r[4]);
            int year = toInt(r[5]);

            LocalDate start = toLocalDate(r[6]);
            LocalDate end = toLocalDate(r[7]);

            BigDecimal net = (r[8] instanceof BigDecimal bd) ? bd : (r[8] == null ? BigDecimal.ZERO : new BigDecimal(r[8].toString()));
            String batchStatus = (String) r[9];

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
                    .netSalary(net == null ? BigDecimal.ZERO : net)
                    .statusLabel(label)
                    .batchStatus(batchStatus)
                    .build();
        }).toList();
    }

    private int toInt(Object v) {
        if (v == null)
            return 0;
        if (v instanceof Number n)
            return n.intValue();     // Integer/Long/BigInteger...
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
                        .employeeName(empName(s.getEmployee()))   // ✅ FIX
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

        // chỉ manager trực tiếp mới xem được
        Integer direct = p.getEmployee().getDirectManagerId();
        if (direct == null || !direct.equals(managerEmpId)) {
            throw new SecurityException("Not allowed to view this payslip");
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
                    end.atTime(LocalTime.MAX)
            );
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

            BigDecimal totalIncome = salaryByDays.add(overtimePay);
            BigDecimal totalDeduction = BigDecimal.ZERO; // bạn có thể thêm tax/insurance sau
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
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        if (!"PENDING_APPROVAL".equals(batch.getStatus())) {
            throw new IllegalStateException("Only PENDING_APPROVAL can be rejected.");
        }
        batch.setStatus("DRAFT");
        batchRepo.save(batch);
        // Nếu bạn muốn lưu reason reject -> mình sẽ thêm table audit log cho bạn.
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

    // =========================
    // EMPLOYEE SIDE
    // =========================

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

    // PDF download for payslip
    @Transactional(readOnly = true)
    public byte[] buildPayslipPdf(Integer empId, Integer payslipId) {
        PayslipDetailDTO dto = getPayslipDetailForEmployee(empId, payslipId);

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(50, 770);
                cs.showText("PAYSLIP");
                cs.endText();

                float y = 740;

                y = writeLine(cs, "Employee: " + dto.getEmployeeName(), y);
                y = writeLine(cs, "Payslip ID: " + dto.getPayslipId(), y);
                y = writeLine(cs, "Net Salary: " + dto.getNetSalary(), y);
                y = writeLine(cs, "---------------------------------------", y);

                y = writeLine(cs, "Items:", y);
                for (PayslipItemDTO it : dto.getItems()) {
                    y = writeLine(cs, "- [" + it.getType() + "] " + it.getName() + ": " + it.getAmount(), y);
                    if (y < 80)
                        break;
                }
            }

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Build PDF failed", e);
        }
    }

    private float writeLine(PDPageContentStream cs, String text, float y) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, y);
        cs.showText(text);
        cs.endText();
        return y - 18;
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
            return mmYY + " (" + per.getStartDate() + " \u2192 " + per.getEndDate() + ")";
        }
        return mmYY;
    }

}
