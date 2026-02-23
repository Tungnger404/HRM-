package com.example.hrm.repository;

import com.example.hrm.entity.EvaluationEvidence;
import com.example.hrm.entity.EvaluationEvidence.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationEvidenceRepository extends JpaRepository<EvaluationEvidence, Integer> {

    List<EvaluationEvidence> findByEvalId(Integer evalId);

    List<EvaluationEvidence> findByEvalIdAndKpiId(Integer evalId, Integer kpiId);

    List<EvaluationEvidence> findByVerificationStatus(VerificationStatus status);
}