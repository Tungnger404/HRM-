package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shift_attendance_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(name = "early_checkin_minutes", nullable = false)
    private Integer earlyCheckinMinutes;

    @Column(name = "late_threshold_minutes", nullable = false)
    private Integer lateThresholdMinutes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}