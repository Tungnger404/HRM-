package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.PerformanceRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceRankingRepository extends JpaRepository<PerformanceRanking, Integer> {

    Optional<PerformanceRanking> findByCycleIdAndEmpId(Integer cycleId, Integer empId);

    List<PerformanceRanking> findByCycleId(Integer cycleId);

    List<PerformanceRanking> findByEmpId(Integer empId);

    List<PerformanceRanking> findByClassification(String classification);

    List<PerformanceRanking> findByCycleIdAndClassification(Integer cycleId, String classification);

    List<PerformanceRanking> findByIsTrainingRequiredTrue();

    List<PerformanceRanking> findByIsPromotionEligibleTrue();
}