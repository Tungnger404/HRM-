package com.example.hrm.repository;

import com.example.hrm.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Integer> {
    Optional<ShiftTemplate> findByShiftCode(String shiftCode);
}
