package com.example.hrm.repository;

import com.example.hrm.entity.ShiftAttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftAttendanceRuleRepository extends JpaRepository<ShiftAttendanceRule, Integer> {
    Optional<ShiftAttendanceRule> findByShiftTemplate_ShiftId(Integer shiftId);
    Optional<ShiftAttendanceRule> findByShiftTemplate_ShiftCode(String shiftCode);
    Optional<ShiftAttendanceRule> findByShiftTemplate_ShiftIdAndIsActiveTrue(Integer shiftId);
}