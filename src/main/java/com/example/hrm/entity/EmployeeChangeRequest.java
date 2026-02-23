package com.example.hrm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_change_requests")
public class EmployeeChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey; // phone/address/identityCard/taxCode

    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    // PENDING / APPROVED / REJECTED
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "approver_user_id")
    private Integer approverUserId;

    @Column(name = "decision_note", columnDefinition = "NVARCHAR(MAX)")
    private String decisionNote;

    public Integer getId() { return id; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public Integer getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Integer approverUserId) { this.approverUserId = approverUserId; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
