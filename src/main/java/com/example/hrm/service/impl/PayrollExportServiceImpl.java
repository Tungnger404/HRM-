package com.example.hrm.service.impl;

import com.example.hrm.dto.PayslipDetailDTO;
import com.example.hrm.dto.PayslipItemDTO;
import com.example.hrm.dto.PayslipSummaryDTO;
import com.example.hrm.entity.BankAccount;
import com.example.hrm.entity.PayrollBatch;
import com.example.hrm.entity.Payslip;
import com.example.hrm.repository.BankAccountRepository;
import com.example.hrm.repository.PayrollBatchRepository;
import com.example.hrm.repository.PayslipRepository;
import com.example.hrm.service.PayrollEmployeeService;
import com.example.hrm.service.PayrollExportService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollExportServiceImpl implements PayrollExportService {

    private final PayrollBatchRepository batchRepo;
    private final PayslipRepository payslipRepo;
    private final BankAccountRepository bankAccountRepo;
    private final PayrollEmployeeService payrollEmployeeService;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportBatchExcel(Integer batchId) {
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

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Payroll");

            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("PayslipId");
            h.createCell(1).setCellValue("EmpId");
            h.createCell(2).setCellValue("Employee");
            h.createCell(3).setCellValue("TotalIncome");
            h.createCell(4).setCellValue("TotalDeduction");
            h.createCell(5).setCellValue("NetSalary");
            h.createCell(6).setCellValue("Batch");
            h.createCell(7).setCellValue("Status");

            int r = 1;
            for (PayslipSummaryDTO s : slips) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getPayslipId());
                row.createCell(1).setCellValue(s.getEmpId());
                row.createCell(2).setCellValue(s.getEmployeeName());
                row.createCell(3).setCellValue(nz(s.getTotalIncome()).doubleValue());
                row.createCell(4).setCellValue(nz(s.getTotalDeduction()).doubleValue());
                row.createCell(5).setCellValue(nz(s.getNetSalary()).doubleValue());
                row.createCell(6).setCellValue(batch.getName() == null ? "" : batch.getName());
                row.createCell(7).setCellValue(batch.getStatus() == null ? "" : batch.getStatus());
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Export Excel failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportBankTransferExcel(List<Integer> batchIds) {
        List<Payslip> slips = batchIds.stream()
                .flatMap(id -> payslipRepo.findByBatch_IdOrderByIdAsc(id).stream())
                .filter(s -> !"REJECTED".equalsIgnoreCase(s.getSlipStatus()))
                .toList();

        Map<Integer, List<Payslip>> byEmp = slips.stream()
                .collect(Collectors.groupingBy(s -> s.getEmployee().getId()));

        List<Integer> empIds = byEmp.keySet().stream().toList();

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

    @Override
    @Transactional(readOnly = true)
    public byte[] buildPayslipPdf(Integer empId, Integer payslipId) {
        PayslipDetailDTO dto = payrollEmployeeService.getPayslipDetailForEmployee(empId, payslipId);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType0Font font = tryLoadTtf(doc, "static/fonts/arial.ttf");
            PDType0Font fontBold = tryLoadTtf(doc, "static/fonts/arialbd.ttf");
            boolean unicodeOk = (font != null && fontBold != null);

            PDFont f = unicodeOk ? font : PDType1Font.HELVETICA;
            PDFont fb = unicodeOk ? fontBold : PDType1Font.HELVETICA_BOLD;

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
                        if (y < 60) break;
                    }
                }
            }

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Build PDF failed", e);
        }
    }

    private static String ascii(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}+", "");
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

    private String empName(com.example.hrm.entity.Employee e) {
        if (e == null) return "";
        String n = e.getFullName();
        if (n != null && !n.trim().isEmpty()) return n.trim();
        return "NV" + e.getId();
    }
}