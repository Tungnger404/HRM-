package com.example.hrm.repository;

import com.example.hrm.entity.TrainingAssignment;  // ✅ THÊM IMPORT NÀY
import com.example.hrm.entity.TrainingAssignment.AssignmentStatus;  // ✅ THÊM IMPORT NÀY
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Integer> {

    List<TrainingAssignment> findByEmpId(Integer empId);

    List<TrainingAssignment> findByEmpIdAndStatus(Integer empId, AssignmentStatus status);

    // Find all assignments where someone is acting as mentor
    List<TrainingAssignment> findByMentorId(Integer mentorId);

    List<TrainingAssignment> findByStatus(AssignmentStatus status);

    List<TrainingAssignment> findByProgramId(Integer programId);

    // COURSE, MENTORING, WORKSHOP
    List<TrainingAssignment> findByTrainingType(String trainingType);

    // Find active mentor assignment for an employee
    Optional<TrainingAssignment> findByEmpIdAndTrainingTypeAndStatus(
            Integer empId, String trainingType, AssignmentStatus status);
}