package com.example.hrm.service;

import com.example.hrm.entity.*;
import com.example.hrm.entity.TrainingAssignment.AssignmentStatus;
import com.example.hrm.entity.TrainingCertificate.CertificateStatus;
import com.example.hrm.entity.TrainingProgress.ProgressStatus;
import com.example.hrm.entity.TrainingProgram.TrainingStatus;
import com.example.hrm.entity.TrainingRecommendation.RecommendationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {

    // =========================================================
    // Training Program
    // =========================================================

    TrainingProgram createTrainingProgram(TrainingProgram trainingProgram);

    TrainingProgram updateTrainingProgram(Integer programId, TrainingProgram trainingProgram);  // Changed Long to Integer

    List<TrainingProgram> getAllTrainingPrograms();

    List<TrainingProgram> getTrainingProgramsByStatus(TrainingStatus status);

    Optional<TrainingProgram> getTrainingProgramById(Integer programId);  // Changed Long to Integer

    List<TrainingProgram> getTrainingProgramsBySkill(String skillCategory);

    // =========================================================
    // Training Assignment
    // =========================================================

    TrainingAssignment assignTraining(Integer empId, Integer programId, Integer assignedBy, String objective,
                                      LocalDate startDate, LocalDate endDate);

    TrainingAssignment createManagerAssignment(Integer empId,
                                               Integer programId,
                                               String programName,
                                               String courseUrl,
                                               Integer assignedBy,
                                               String objective,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               String trainingType,
                                               Integer recommendationId);

    TrainingAssignment assignMentor(Integer empId, Integer mentorId, Integer assignedBy, String objective);

    TrainingAssignment updateAssignmentStatus(Integer assignId, AssignmentStatus newStatus);

    List<TrainingAssignment> getAssignmentsByEmployee(Integer empId);

    List<TrainingAssignment> getAssignmentsByMentor(Integer mentorId);

    List<TrainingAssignment> getAssignmentsByProgram(Integer programId);

    // =========================================================
    // Training Progress
    // =========================================================

    TrainingProgress createProgress(Integer assignId, Integer empId, Integer programId);

    TrainingProgress updateProgress(Integer progressId, BigDecimal completionPercentage, ProgressStatus status);

    TrainingProgress updateProgressScore(Integer progressId, BigDecimal finalScore, BigDecimal attendanceRate);

    /**
     * Employee marks the training as completed.
     * Status: IN_PROGRESS -> AWAITING_EVIDENCE
     * System requires certificate upload.
     */
    TrainingProgress markTrainingAsComplete(Integer progressId);

    List<TrainingProgress> getProgressByEmployee(Integer empId);

    List<TrainingProgress> getProgressByProgram(Integer programId);

    Optional<TrainingProgress> getProgressByEmployeeAndProgram(Integer empId, Integer programId);

    // =========================================================
    // Training Certificate
    // =========================================================

    TrainingCertificate uploadCertificate(Integer empId, Integer programId, String certificateName, String fileUrl);

    TrainingCertificate verifyCertificate(Integer certId, Boolean isValid, Integer verifiedBy, String verificationNote);

    List<TrainingCertificate> getCertificatesByEmployee(Integer empId);

    List<TrainingCertificate> getCertificatesByProgram(Integer programId);

    List<TrainingCertificate> getPendingCertificates();

    // =========================================================
    // Training Recommendation
    // =========================================================

    TrainingRecommendation createRecommendation(Integer empId, Integer evalId, Integer programId,
                                                String reason, String priority, Integer recommendedBy);

    TrainingRecommendation updateRecommendationStatus(Integer recommendationId, RecommendationStatus newStatus);

    List<TrainingRecommendation> getRecommendationsByEmployee(Integer empId);
}