package com.example.hrm.service;

import com.example.hrm.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    List<Contract> list(Integer empId);

    Contract create(Integer empId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status);

    Contract update(Integer contractId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status);

    void terminate(Integer contractId);

    Contract get(Integer contractId);
}
