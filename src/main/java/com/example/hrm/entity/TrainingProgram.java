package com.example.hrm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_programs")
public class TrainingProgram {

    public enum TrainingStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Integer programId;

    @Column(name = "program_code", unique = true, length = 50)
    private String programCode;

    @Column(name = "program_name", nullable = false, length = 200)
    private String programName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "skill_category", length = 100)
    private String skillCategory;

    @Column(name = "level", length = 50)
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TrainingStatus status = TrainingStatus.ACTIVE;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }

    public String getProgramCode() { return programCode; }
    public void setProgramCode(String programCode) { this.programCode = programCode; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public String getSkillCategory() { return skillCategory; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public TrainingStatus getStatus() { return status; }
    public void setStatus(TrainingStatus status) { this.status = status; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
