package com.example.hrm.repository;

import com.example.hrm.entity.Evaluation;
import com.example.hrm.entity.Evaluation.EvaluationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    List<Evaluation> findByEmpId(Integer empId);

    Optional<Evaluation> findByEmpIdAndCycleId(Integer empId, Integer cycleId);

    List<Evaluation> findByStatus(EvaluationStatus status);

    List<Evaluation> findByManagerIdAndStatus(Integer managerId, EvaluationStatus status);

    List<Evaluation> findByClassification(String classification);

    List<Evaluation> findByCycleId(Integer cycleId);

    List<Evaluation> findByCycleIdAndStatus(Integer cycleId, EvaluationStatus status);

    List<Evaluation> findByEmpIdAndStatus(Integer empId, EvaluationStatus status);

    List<Evaluation> findByEmpIdOrderByEvalIdDesc(Integer empId);
}