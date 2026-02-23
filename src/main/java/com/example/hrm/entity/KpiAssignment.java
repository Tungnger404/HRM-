package com.example.hrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_assignments")
public class KpiAssignment {

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

    @Override
    public String toString() {
        return "KpiAssignment{" +
                "assignmentId=" + assignmentId +
                ", cycleId=" + cycleId +
                ", kpiId=" + kpiId +
                ", empId=" + empId +
                ", deptId=" + deptId +
                '}';
    }
}
