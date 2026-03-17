package com.example.hrm.service;

import com.example.hrm.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    List<Contract> list(Integer empId);

    List<Contract> listExpiringWithin30Days();

    Contract create(Integer empId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status);

    Contract update(Integer contractId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status);

    Contract updateDetail(Integer contractId,
                          LocalDate startDate,
                          LocalDate endDate,
                          BigDecimal baseSalary,
                          String status,
                          String contractType);

    void terminate(Integer contractId);

    void approveOfficial(Integer contractId);

    void rejectEmployee(Integer contractId);

    Contract get(Integer contractId);
}