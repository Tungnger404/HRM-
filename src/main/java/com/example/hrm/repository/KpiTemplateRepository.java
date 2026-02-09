package com.example.hrm.repository;

import com.example.hrm.entity.KpiTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiTemplateRepository extends JpaRepository<KpiTemplate, Integer> {

    Optional<KpiTemplate> findByKpiName(String kpiName);

    List<KpiTemplate> findByCreatedBy(Integer createdBy);

    List<KpiTemplate> findAllByOrderByKpiNameAsc();
}