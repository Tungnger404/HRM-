package com.example.hrm.service;

import com.example.hrm.dto.BenefitUpsertDTO;
import com.example.hrm.entity.Benefit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BenefitService {

    record BenefitApplied(String code, String name, String type, BigDecimal amount) {}

    List<Benefit> list(String q, String type);

    Benefit create(BenefitUpsertDTO dto);

    Benefit update(Integer id, BenefitUpsertDTO dto);

    void delete(Integer id);

    void assignBenefitToAllEmployees(Integer benefitId,
                                     LocalDate effectiveFrom,
                                     LocalDate effectiveTo,
                                     BigDecimal overrideValue);

    List<BenefitApplied> calculateForEmployee(Integer empId,
                                              LocalDate start,
                                              LocalDate end,
                                              BigDecimal baseSalary,
                                              BigDecimal actualWorkDays);

    List<Benefit> listActive();

    Benefit requireActive(Integer id);
}