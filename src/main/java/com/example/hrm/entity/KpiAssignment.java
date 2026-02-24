package com.example.hrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_assignments")
public class KpiAssignment {

    public enum AssignmentStatus {
        ASSIGNED,
        DRAFT,
        EMPLOYEE_SUBMITTED,
        HR_REJECTED,
        EMPLOYEE_RESUBMITTED,
        HR_VERIFIED,
        MANAGER_REJECTED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @Column(name = "cycle_id", nullable = false)
    private Integer cycleId;

    @Column(name = "kpi_id", nullable = false)
    private Integer kpiId;

    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "min_threshold", precision = 10, scale = 2)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", precision = 10, scale = 2)
    private BigDecimal maxThreshold;

    @Column(name = "assigned_by")
    private Integer assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "hr_excel_template_path", length = 255)
    private String hrExcelTemplatePath;

    @Column(name = "hr_comment", columnDefinition = "NVARCHAR(MAX)")
    private String hrComment;

    @Column(name = "employee_excel_path", length = 255)
    private String employeeExcelPath;

    @Column(name = "employee_comment", columnDefinition = "NVARCHAR(MAX)")
    private String employeeComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "employee_submitted_at")
    private LocalDateTime employeeSubmittedAt;

    @Column(name = "hr_verified_at")
    private LocalDateTime hrVerifiedAt;

    @Column(name = "hr_verified_by")
    private Integer hrVerifiedBy;

    @Column(name = "hr_verification_note", columnDefinition = "NVARCHAR(MAX)")
    private String hrVerificationNote;

    // Constructors
    public KpiAssignment() {}

    public KpiAssignment(Integer cycleId, Integer kpiId, Integer empId, Integer deptId,
                         BigDecimal targetValue, BigDecimal minThreshold, BigDecimal maxThreshold,
                         Integer assignedBy, LocalDateTime assignedAt) {
        this.cycleId = cycleId;
        this.kpiId = kpiId;
        this.empId = empId;
        this.deptId = deptId;
        this.targetValue = targetValue;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }

    public Integer getCycleId() { return cycleId; }
    public void setCycleId(Integer cycleId) { this.cycleId = cycleId; }

    public Integer getKpiId() { return kpiId; }
    public void setKpiId(Integer kpiId) { this.kpiId = kpiId; }

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }

    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }

    public Integer getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Integer assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getHrExcelTemplatePath() { return hrExcelTemplatePath; }
    public void setHrExcelTemplatePath(String hrExcelTemplatePath) { this.hrExcelTemplatePath = hrExcelTemplatePath; }

    public String getHrComment() { return hrComment; }
    public void setHrComment(String hrComment) { this.hrComment = hrComment; }

    public String getEmployeeExcelPath() { return employeeExcelPath; }
    public void setEmployeeExcelPath(String employeeExcelPath) { this.employeeExcelPath = employeeExcelPath; }

    public String getEmployeeComment() { return employeeComment; }
    public void setEmployeeComment(String employeeComment) { this.employeeComment = employeeComment; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getEmployeeSubmittedAt() { return employeeSubmittedAt; }
    public void setEmployeeSubmittedAt(LocalDateTime employeeSubmittedAt) { this.employeeSubmittedAt = employeeSubmittedAt; }

    public LocalDateTime getHrVerifiedAt() { return hrVerifiedAt; }
    public void setHrVerifiedAt(LocalDateTime hrVerifiedAt) { this.hrVerifiedAt = hrVerifiedAt; }

    public Integer getHrVerifiedBy() { return hrVerifiedBy; }
    public void setHrVerifiedBy(Integer hrVerifiedBy) { this.hrVerifiedBy = hrVerifiedBy; }

    public String getHrVerificationNote() { return hrVerificationNote; }
    public void setHrVerificationNote(String hrVerificationNote) { this.hrVerificationNote = hrVerificationNote; }

    @Override
    public String toString() {
        return "KpiAssignment{" +
                "assignmentId=" + assignmentId +
                ", cycleId=" + cycleId +
                ", kpiId=" + kpiId +
                ", empId=" + empId +
                ", deptId=" + deptId +
                ", status=" + status +
                '}';
    }
}
