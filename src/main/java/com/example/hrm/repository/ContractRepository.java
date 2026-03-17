package com.example.hrm.repository;

import com.example.hrm.entity.Contract;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    @Query("""
        select c from Contract c
        where c.employee.empId = :empId
          and c.status = 'ACTIVE'
          and c.startDate <= :periodEnd
          and (c.endDate is null or c.endDate >= :periodStart)
        order by c.startDate desc
    """)
    Optional<Contract> findActiveContractForPeriod(@Param("empId") Integer empId,
                                                   @Param("periodStart") LocalDate periodStart,
                                                   @Param("periodEnd") LocalDate periodEnd);

    List<Contract> findByEmployee_EmpIdOrderByStartDateDesc(Integer empId);

    @Query("select c from Contract c order by c.id desc")
    List<Contract> findAllNewest();

    List<Contract> findByStatusOrderByStartDateDesc(String status);

    @Query("""
        select c from Contract c
        where c.employee.empId = :empId
          and c.status = 'ACTIVE'
        order by c.startDate desc
    """)
    List<Contract> findActiveContracts(@Param("empId") Integer empId);

    long countByEndDateBetween(LocalDate start, LocalDate end);

    @Query("""
        select count(c) from Contract c
        where c.status = 'ACTIVE'
          and c.endDate is not null
          and c.endDate between :today and :next30
    """)
    long countExpiringActiveContracts(@Param("today") LocalDate today,
                                      @Param("next30") LocalDate next30);

    @Query("""
        select c from Contract c
        where c.status = 'ACTIVE'
          and c.endDate is not null
          and c.endDate between :today and :next30
        order by c.endDate asc, c.employee.empId asc
    """)
    List<Contract> findExpiringActiveContracts(@Param("today") LocalDate today,
                                               @Param("next30") LocalDate next30);
}