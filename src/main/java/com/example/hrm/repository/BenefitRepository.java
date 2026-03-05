package com.example.hrm.repository;

import com.example.hrm.entity.Benefit;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BenefitRepository extends JpaRepository<Benefit, Integer> {

    boolean existsByCodeIgnoreCase(String code);

    List<Benefit> findByActiveTrueOrderByTypeAscCodeAsc();

    @Query("""
        select b from Benefit b
        where (:q is null or :q = '' 
               or upper(b.code) like concat('%', upper(:q), '%')
               or upper(b.name) like concat('%', upper(:q), '%'))
          and (:type is null or :type = '' or b.type = :type)
        order by b.active desc, b.type asc, b.code asc
    """)
    List<Benefit> search(@Param("q") String q, @Param("type") String type);

    @Query("""
        select b from Benefit b
        where b.active = true
          and b.effectiveFrom <= :end
          and (b.effectiveTo is null or b.effectiveTo >= :start)
        order by b.type asc, b.code asc
    """)
    List<Benefit> findEffective(@Param("start") LocalDate start, @Param("end") LocalDate end);
}