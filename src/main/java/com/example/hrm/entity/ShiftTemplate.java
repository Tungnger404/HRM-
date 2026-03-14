package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name ="shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Integer shiftId;

    @Column(name = "shift_code", nullable = false, unique = true, length = 20)
    private String shiftCode; // DAY, EVENING, NIGHT

    @Column(name = "shift_name", nullable = false, length = 100)
    private String shiftName; // Day Shift, Evening Shift, Night Shift

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_overnight", nullable = false)
    private Boolean isOvernight;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
