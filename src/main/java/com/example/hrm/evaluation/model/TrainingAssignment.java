package com.example.hrm.evaluation.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "training_assignments")
public class TrainingAssignment {

    public enum AssignmentStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assign_id")
    private Integer assignId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "program_id")
    private Integer programId;

    @Column(name = "program_name", length = 200)
    private String programName;

    @Column(name = "mentor_id")
    private Integer mentorId;

    @Column(name = "training_type", length = 50)
    private String trainingType; // COURSE, MENTORING, WORKSHOP

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AssignmentStatus status = AssignmentStatus.PLANNED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "assigned_by")
    private Integer assignedBy;

    @Column(name = "objective", columnDefinition = "NVARCHAR(MAX)")
    private String objective;

    @Column(name = "completion_note", columnDefinition = "NVARCHAR(MAX)")
    private String completionNote;

    // Getters and Setters
    public Integer getAssignId() {
        return assignId;
    }

    public void setAssignId(Integer assignId) {
        this.assignId = assignId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Integer getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Integer assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public void setCompletionNote(String completionNote) {
        this.completionNote = completionNote;
    }
}