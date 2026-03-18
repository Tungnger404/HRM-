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
import java.time.LocalDateTime;
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
        if (empId != null) {
            return contractRepo.findByEmployee_EmpIdOrderByStartDateDesc(empId);
        }
        return contractRepo.findAllNewest();
    }

    @Override
    public List<Contract> listByEmployee(Integer empId) {
        return contractRepo.findByEmployee_EmpIdOrderByStartDateDesc(empId);
    }

    @Override
    public List<Contract> listExpiringWithin30Days() {
        LocalDate today = LocalDate.now();
        LocalDate next30 = today.plusDays(30);
        return contractRepo.findExpiringActiveContracts(today, next30);
    }

    @Override
    @Transactional
    public Contract create(Integer empId,
                           String contractNumber,
                           String contractType,
                           LocalDate startDate,
                           LocalDate endDate,
                           BigDecimal baseSalary,
                           String status) {

        if (empId == null) throw new IllegalArgumentException("empId is required");
        if (startDate == null) throw new IllegalArgumentException("startDate is required");
        if (baseSalary == null) throw new IllegalArgumentException("baseSalary is required");

        Employee e = employeeRepo.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empId));

        String normalizedType = normalizeContractType(contractType, endDate);
        String normalizedStatus = (status == null || status.isBlank()) ? "ACTIVE" : status.trim().toUpperCase();

        if ("OFFICIAL_INDEFINITE".equalsIgnoreCase(normalizedType)) {
            endDate = null;
        } else if ("OFFICIAL_1_YEAR".equalsIgnoreCase(normalizedType) && startDate != null) {
            endDate = startDate.plusYears(1);
        }

        if ("ACTIVE".equalsIgnoreCase(normalizedStatus)) {
            contractRepo.terminateAllActiveContractsByEmpId(empId);
        }

        Contract c = new Contract();
        c.setEmployee(e);
        c.setContractNumber(buildContractNumber(empId, contractNumber));
        c.setContractType(normalizedType);
        c.setStartDate(startDate);
        c.setEndDate(endDate);
        c.setBaseSalary(baseSalary);
        c.setStatus(normalizedStatus);

        return contractRepo.save(c);
    }

    @Override
    @Transactional
    public Contract update(Integer contractId,
                           LocalDate startDate,
                           LocalDate endDate,
                           BigDecimal baseSalary,
                           String status) {

        Contract c = get(contractId);

        if (startDate != null) c.setStartDate(startDate);
        c.setEndDate(endDate);
        if (baseSalary != null) c.setBaseSalary(baseSalary);
        if (status != null && !status.isBlank()) c.setStatus(status.trim().toUpperCase());

        if (c.getEndDate() == null && !"PROBATION".equalsIgnoreCase(c.getContractType())) {
            c.setContractType("OFFICIAL_INDEFINITE");
        } else if (c.getEndDate() != null && !"PROBATION".equalsIgnoreCase(c.getContractType())) {
            c.setContractType("OFFICIAL_1_YEAR");
        }

        return contractRepo.save(c);
    }

    @Override
    @Transactional
    public Contract updateDetail(Integer contractId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 BigDecimal baseSalary,
                                 String status,
                                 String contractType,
                                 String contractNumber) {

        Contract c = get(contractId);

        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (baseSalary == null) {
            throw new IllegalArgumentException("baseSalary is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        if (contractType == null || contractType.isBlank()) {
            throw new IllegalArgumentException("contractType is required");
        }

        String normalizedStatus = status.trim().toUpperCase();
        String normalizedType = normalizeContractType(contractType, endDate);

        c.setStartDate(startDate);
        c.setBaseSalary(baseSalary);
        c.setStatus(normalizedStatus);
        c.setContractType(normalizedType);
        c.setContractNumber(buildContractNumber(c.getEmployee().getEmpId(), contractNumber));

        if ("OFFICIAL_INDEFINITE".equalsIgnoreCase(normalizedType)) {
            c.setEndDate(null);
        } else if ("OFFICIAL_1_YEAR".equalsIgnoreCase(normalizedType)) {
            c.setEndDate(startDate.plusYears(1));
        } else {
            c.setEndDate(endDate);
        }

        return contractRepo.save(c);
    }

    @Override
    @Transactional
    public void terminate(Integer contractId) {
        Contract c = get(contractId);

        c.setStatus("TERMINATED");
        if (c.getEndDate() == null) {
            c.setEndDate(LocalDate.now());
        }
        contractRepo.save(c);
    }

    @Override
    @Transactional
    public void approveOfficial(Integer contractId) {
        Contract c = get(contractId);

        Employee e = c.getEmployee();
        e.setStatus("ACTIVE");
        employeeRepo.save(e);

        c.setStatus("ACTIVE");
        c.setEndDate(null);
        c.setContractType("OFFICIAL_INDEFINITE");

        contractRepo.terminateAllActiveContractsByEmpId(e.getEmpId());
        contractRepo.save(c);
    }

    @Override
    @Transactional
    public void rejectEmployee(Integer contractId) {
        Contract c = get(contractId);

        c.setStatus("TERMINATED");
        if (c.getEndDate() == null) {
            c.setEndDate(LocalDate.now());
        }
        contractRepo.save(c);

        Employee e = c.getEmployee();
        e.setStatus("PROBATION");
        employeeRepo.save(e);
    }

    @Override
    public Contract get(Integer contractId) {
        return contractRepo.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));
    }

    private String normalizeContractType(String contractType, LocalDate endDate) {
        if (contractType == null || contractType.isBlank()) {
            return endDate == null ? "OFFICIAL_INDEFINITE" : "OFFICIAL_1_YEAR";
        }

        String type = contractType.trim().toUpperCase();

        if (!type.equals("PROBATION")
                && !type.equals("OFFICIAL_1_YEAR")
                && !type.equals("OFFICIAL_INDEFINITE")) {
            throw new IllegalArgumentException("Invalid contract type: " + contractType);
        }

        return type;
    }

    private String buildContractNumber(Integer empId, String contractNumber) {
        if (contractNumber != null && !contractNumber.isBlank()) {
            return contractNumber.trim();
        }
        return "CT-" + empId + "-" + LocalDateTime.now().toString().replace(":", "").replace(".", "");
    }
}