package com.example.hrm.repository;

import com.example.hrm.entity.TrainingProgress;
import com.example.hrm.entity.TrainingProgress.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProgressRepository extends JpaRepository<TrainingProgress, Integer> {

    List<TrainingProgress> findByEmpId(Integer empId);

    List<TrainingProgress> findByProgramId(Integer programId);

    List<TrainingProgress> findByAssignId(Integer assignId);

    List<TrainingProgress> findByStatus(ProgressStatus status);

    Optional<TrainingProgress> findByEmpIdAndProgramId(Integer empId, Integer programId);

    Optional<TrainingProgress> findByAssignIdAndEmpIdAndProgramId(
            Integer assignId, Integer empId, Integer programId);
}