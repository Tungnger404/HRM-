package com.example.hrm.service.impl;

import com.example.hrm.entity.Contract;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.ContractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepo;
    private final EmployeeRepository employeeRepo;

    public ContractServiceImpl(ContractRepository contractRepo, EmployeeRepository employeeRepo) {
        this.contractRepo = contractRepo;
        this.employeeRepo = employeeRepo;
    }

    @Override
    public List<Contract> list(Integer empId) {
        if (empId != null) return contractRepo.findByEmployee_EmpIdOrderByStartDateDesc(empId);
        return contractRepo.findAllNewest();
    }

    @Override
    @Transactional
    public Contract create(Integer empId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status) {
        if (empId == null) throw new IllegalArgumentException("empId is required");
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (baseSalary == null) throw new IllegalArgumentException("baseSalary is required");

        Employee e = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));

        Contract c = new Contract();
        c.setEmployee(e);
        c.setStartDate(startDate);
        c.setEndDate(endDate);
        c.setBaseSalary(baseSalary);
        c.setStatus((status == null || status.isBlank()) ? "ACTIVE" : status.trim());
        return contractRepo.save(c);
    }

    @Override
    @Transactional
    public Contract update(Integer contractId, LocalDate startDate, LocalDate endDate, BigDecimal baseSalary, String status) {
        Contract c = get(contractId);
        if (startDate != null) c.setStartDate(startDate);
        c.setEndDate(endDate);
        if (baseSalary != null) c.setBaseSalary(baseSalary);
        if (status != null && !status.isBlank()) c.setStatus(status.trim());
        return contractRepo.save(c);
    }

    @Override
    @Transactional
    public void terminate(Integer contractId) {
        Contract c = get(contractId);
        c.setStatus("TERMINATED");
        contractRepo.save(c);
    }

    @Override
    public Contract get(Integer contractId) {
        return contractRepo.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));
    }
}
