package com.example.hrm.repository;

import com.example.hrm.entity.EmployeeBenefit;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefit, Integer> {

    boolean existsByEmployee_IdAndBenefit_IdAndEffectiveFrom(Integer empId, Integer benefitId, LocalDate effectiveFrom);

    @Query("""
        select eb from EmployeeBenefit eb
        join fetch eb.benefit b
        where eb.employee.id = :empId
          and eb.active = true
          and eb.effectiveFrom <= :end
          and (eb.effectiveTo is null or eb.effectiveTo >= :start)
          and b.active = true
          and b.effectiveFrom <= :end
          and (b.effectiveTo is null or b.effectiveTo >= :start)
        order by b.type asc, b.code asc
    """)
    List<EmployeeBenefit> findEffectiveForEmployee(@Param("empId") Integer empId,
                                                   @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);
}