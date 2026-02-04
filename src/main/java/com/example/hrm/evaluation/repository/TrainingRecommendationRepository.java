package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.TrainingRecommendation;
import com.example.hrm.evaluation.model.TrainingRecommendation.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRecommendationRepository extends JpaRepository<TrainingRecommendation, Integer> {

    List<TrainingRecommendation> findByEmpId(Integer empId);

    List<TrainingRecommendation> findByEvalId(Integer evalId);

    List<TrainingRecommendation> findByStatus(RecommendationStatus status);

    List<TrainingRecommendation> findByEmpIdAndStatus(Integer empId, RecommendationStatus status);

    List<TrainingRecommendation> findByProgramId(Integer programId);
}