package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefits")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "benefit_id")
    private Integer id;

    @Column(name = "benefit_code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "benefit_name", nullable = false, length = 150)
    private String name;

    // INCOME / DEDUCTION
    @Column(name = "benefit_type", nullable = false, length = 20)
    private String type;

    // FIXED / PERCENT_BASE
    @Column(name = "calc_method", nullable = false, length = 20)
    private String calcMethod;

    // FIXED: amount, PERCENT_BASE: 0.03
    @Column(name = "benefit_value", nullable = false, precision = 18, scale = 6)
    private BigDecimal value;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (active == null) active = true;
        if (value == null) value = BigDecimal.ZERO;
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}