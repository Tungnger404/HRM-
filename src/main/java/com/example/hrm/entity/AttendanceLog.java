package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "attendance_logs",
        uniqueConstraints = @UniqueConstraint(name = "uq_emp_date", columnNames = {"emp_id", "work_date"})
)
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "emp_id")
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "status", length = 20)
    private String status; // ON_TIME/LATE/ABSENT/EARLY_LEAVE

    @Column(name="work_type")
    private String workType;
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "shift_id")
    private Integer shiftId;

    @Column(name = "scheduled_start_at")
    private LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private LocalDateTime scheduledEndAt;

    @Column(name = "is_late")
    private Boolean isLate;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "is_early_leave")
    private Boolean isEarlyLeave;

    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes;
}
