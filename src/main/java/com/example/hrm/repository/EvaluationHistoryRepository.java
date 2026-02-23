package com.example.hrm.repository;

import com.example.hrm.entity.EvaluationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationHistoryRepository extends JpaRepository<EvaluationHistory, Integer> {

    List<EvaluationHistory> findByEvalId(Integer evalId);

    List<EvaluationHistory> findByEvalIdOrderByActionAtDesc(Integer evalId);

    List<EvaluationHistory> findByActionBy(Integer actionBy);
}