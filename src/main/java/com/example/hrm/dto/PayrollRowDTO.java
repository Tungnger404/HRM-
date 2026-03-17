package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRowDTO {

    private int payslipId;
    private int batchId;

    private int empId;
    private String empCode;
    private String fullName;

    private int month;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;

    private String jobTitle;
    private String batchName;
    private String slipStatus; // ACTIVE / APPROVED / REJECTED

    // Work metrics
    private BigDecimal baseSalary;        // lương cơ bản/vị trí
    private BigDecimal standardWorkDays;  // ngày công chuẩn
    private BigDecimal actualWorkDays;    // ngày công thực tế
    private BigDecimal otHours;           // OT giờ
    private BigDecimal dailySalary;       // lương ngày (base/standard)

    // Totals
    private BigDecimal totalIncome;       // gross
    private BigDecimal totalDeduction;    // deduction
    private BigDecimal netSalary;         // net

    private String batchStatus;
    private String statusLabel;

    // Enterprise flags
    private Boolean sentToEmployee;  // đã release cho employee chưa
    private Boolean bankMissing;     // thiếu bank account primary hay không

    private String rejectReason;
    private LocalDateTime rejectedAt;

    // ===== Alias cho template cũ =====
    public String getEmployeeName() {
        return this.fullName;
    }

    public void setEmployeeName(String employeeName) {
        this.fullName = employeeName;
    }

    public String getPeriodLabel() {
        if (month > 0 && year > 0) {
            return String.format("%02d/%d", month, year);
        }
        return "";
    }

    public BigDecimal getSavedNet() {
        return this.netSalary;
    }

    public void setSavedNet(BigDecimal savedNet) {
        this.netSalary = savedNet;
    }

    public BigDecimal getSavedGross() {
        return this.totalIncome;
    }

    public void setSavedGross(BigDecimal savedGross) {
        this.totalIncome = savedGross;
    }

    public BigDecimal getSavedDeduction() {
        return this.totalDeduction;
    }

    public void setSavedDeduction(BigDecimal savedDeduction) {
        this.totalDeduction = savedDeduction;
    }

    public BigDecimal getSavedBaseSalary() {
        return this.baseSalary;
    }

    public void setSavedBaseSalary(BigDecimal savedBaseSalary) {
        this.baseSalary = savedBaseSalary;
    }
}