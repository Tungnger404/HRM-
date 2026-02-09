package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.EvaluationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationDetailRepository extends JpaRepository<EvaluationDetail, Integer> {

    List<EvaluationDetail> findByEvalId(Integer evalId);

    Optional<EvaluationDetail> findByEvalIdAndKpiId(Integer evalId, Integer kpiId);

    List<EvaluationDetail> findByKpiId(Integer kpiId);
}