package com.example.hrm.repository;

import com.example.hrm.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    @Query("""
        select c from Contract c
        where c.employee.id = :empId
          and c.status = 'ACTIVE'
          and c.startDate <= :periodEnd
          and (c.endDate is null or c.endDate >= :periodStart)
        order by c.startDate desc
    """)
    Optional<Contract> findActiveContractForPeriod(Integer empId, LocalDate periodStart, LocalDate periodEnd);
}
