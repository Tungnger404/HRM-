package com.example.hrm.controller;

import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/hr/team-timesheet")
@RequiredArgsConstructor
public class HrTeamTimesheetController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    @GetMapping
    public String viewTeamTimesheet(@RequestParam(value = "month", required = false) String monthStr,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    Model model) {

        YearMonth targetMonth;
        if (monthStr != null && !monthStr.isEmpty()) {
            targetMonth = YearMonth.parse(monthStr);
        } else {
            targetMonth = YearMonth.now();
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<Employee> employees = employeeRepository.search(keyword, null);

        List<AttendanceLog> logs = attendanceLogRepository.findLogsInDateRange(startDate, endDate);
        
        Map<Integer, Map<LocalDate, AttendanceLog>> groupedLogs = logs.stream()
                .filter(log -> log.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getEmployee().getEmpId(),
                        Collectors.toMap(AttendanceLog::getWorkDate, l -> l, (a, b) -> a)
                ));

        List<LocalDate> daysInMonth = startDate.datesUntil(endDate.plusDays(1)).toList();

        Map<Integer, Map<LocalDate, String>> attendanceMatrix = new HashMap<>();
        Map<Integer, Map<String, Integer>> summaryMatrix = new HashMap<>();

        for (Employee emp : employees) {
            Map<LocalDate, String> empDays = new HashMap<>();
            Map<String, Integer> empSummary = new HashMap<>();
            empSummary.put("PRESENT", 0);
            empSummary.put("LATE", 0);
            empSummary.put("VACATION", 0);
            empSummary.put("ABSENT", 0);

            Map<LocalDate, AttendanceLog> empLogs = groupedLogs.getOrDefault(emp.getEmpId(), Collections.emptyMap());

            for (LocalDate day : daysInMonth) {
                if (day.isAfter(LocalDate.now())) {
                    empDays.put(day, ""); // future, no data
                    continue;
                }

                AttendanceLog log = empLogs.get(day);
                String code = "";
                
                if (log != null) {
                    if ("LEAVE".equalsIgnoreCase(log.getStatus()) || "LEAVE".equalsIgnoreCase(log.getWorkType())) {
                        code = "V";
                        empSummary.put("VACATION", empSummary.get("VACATION") + 1);
                    } else if ("LATE".equalsIgnoreCase(log.getStatus())) {
                        code = "L";
                        empSummary.put("LATE", empSummary.get("LATE") + 1);
                        empSummary.put("PRESENT", empSummary.get("PRESENT") + 1); 
                    } else if ("ABSENT".equalsIgnoreCase(log.getStatus())) {
                        code = "A";
                        empSummary.put("ABSENT", empSummary.get("ABSENT") + 1);
                    } else {
                        code = "P"; 
                        empSummary.put("PRESENT", empSummary.get("PRESENT") + 1);
                    }
                } else {
                    code = "-"; 
                }
                empDays.put(day, code);
            }
            attendanceMatrix.put(emp.getEmpId(), empDays);
            summaryMatrix.put(emp.getEmpId(), empSummary);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("attendanceMatrix", attendanceMatrix);
        model.addAttribute("summaryMatrix", summaryMatrix);
        model.addAttribute("keyword", keyword);

        return "hr/team-timesheet";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportTimesheet(
            @RequestParam(value = "month", required = false) String monthStr,
            @RequestParam(value = "keyword", required = false) String keyword) throws IOException {

        YearMonth targetMonth;
        if (monthStr != null && !monthStr.isEmpty()) {
            targetMonth = YearMonth.parse(monthStr);
        } else {
            targetMonth = YearMonth.now();
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<Employee> employees = employeeRepository.search(keyword, null);
        List<AttendanceLog> logs = attendanceLogRepository.findLogsInDateRange(startDate, endDate);
        
        Map<Integer, Map<LocalDate, AttendanceLog>> groupedLogs = logs.stream()
                .filter(log -> log.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getEmployee().getEmpId(),
                        Collectors.toMap(AttendanceLog::getWorkDate, l -> l, (a, b) -> a)
                ));

        List<LocalDate> daysInMonth = startDate.datesUntil(endDate.plusDays(1)).toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Timesheet " + targetMonth.toString());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Employee ID");
            headerRow.createCell(1).setCellValue("Employee Name");
            headerRow.createCell(2).setCellValue("Job Position");
            
            int colIdx = 3;
            for (LocalDate day : daysInMonth) {
                headerRow.createCell(colIdx++).setCellValue(String.valueOf(day.getDayOfMonth()));
            }
            
            headerRow.createCell(colIdx++).setCellValue("Total Present");
            headerRow.createCell(colIdx++).setCellValue("Total Late");
            headerRow.createCell(colIdx++).setCellValue("Total Absent");
            headerRow.createCell(colIdx).setCellValue("Total Leave");

            for (int i = 0; i <= colIdx; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(emp.getEmpId());
                row.createCell(1).setCellValue(emp.getFullName());
                row.createCell(2).setCellValue(emp.getJobPosition() != null ? emp.getJobPosition().getTitle() : "Employee");

                Map<LocalDate, AttendanceLog> empLogs = groupedLogs.getOrDefault(emp.getEmpId(), Collections.emptyMap());
                
                int present = 0, late = 0, absent = 0, vacation = 0;
                int currentCell = 3;
                
                for (LocalDate day : daysInMonth) {
                    if (day.isAfter(LocalDate.now())) {
                        row.createCell(currentCell++).setCellValue("");
                        continue;
                    }

                    AttendanceLog log = empLogs.get(day);
                    String code = "-";
                    
                    if (log != null) {
                        if ("LEAVE".equalsIgnoreCase(log.getStatus()) || "LEAVE".equalsIgnoreCase(log.getWorkType())) {
                            code = "V"; vacation++;
                        } else if ("LATE".equalsIgnoreCase(log.getStatus())) {
                            code = "L"; late++; present++;
                        } else if ("ABSENT".equalsIgnoreCase(log.getStatus())) {
                            code = "A"; absent++;
                        } else {
                            code = "P"; present++;
                        }
                    }
                    
                    Cell cell = row.createCell(currentCell++);
                    cell.setCellValue(code);
                    cell.setCellStyle(centerStyle);
                }
                
                row.createCell(currentCell++).setCellValue(present);
                row.createCell(currentCell++).setCellValue(late);
                row.createCell(currentCell++).setCellValue(absent);
                row.createCell(currentCell).setCellValue(vacation);
            }

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timesheet_" + targetMonth.toString() + ".xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        }
    }
}
