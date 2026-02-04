package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.TrainingProgram;
import com.example.hrm.evaluation.model.TrainingProgram.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Integer> {

    Optional<TrainingProgram> findByProgramCode(String programCode);

    List<TrainingProgram> findByStatus(TrainingStatus status);

    List<TrainingProgram> findBySkillCategory(String skillCategory);

    List<TrainingProgram> findByLevel(String level);
}