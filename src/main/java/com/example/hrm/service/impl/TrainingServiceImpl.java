package com.example.hrm.evaluation.service.impl;

import com.example.hrm.entity.*;
import com.example.hrm.entity.TrainingAssignment.AssignmentStatus;
import com.example.hrm.entity.TrainingCertificate.CertificateStatus;
import com.example.hrm.entity.TrainingProgress.ProgressStatus;
import com.example.hrm.entity.TrainingRecommendation.RecommendationStatus;
import com.example.hrm.repository.*;
import com.example.hrm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;

    @Autowired
    private TrainingAssignmentRepository trainingAssignmentRepository;

    @Autowired
    private TrainingProgressRepository trainingProgressRepository;

    @Autowired
    private TrainingCertificateRepository trainingCertificateRepository;

    @Autowired
    private TrainingRecommendationRepository trainingRecommendationRepository;

    // === Training Program ===

    @Override
    @Transactional
    public TrainingProgram createTrainingProgram(TrainingProgram trainingProgram) {
        trainingProgram.setCreatedAt(LocalDateTime.now());
        trainingProgram.setStatus(TrainingProgram.TrainingStatus.ACTIVE);
        return trainingProgramRepository.save(trainingProgram);
    }

    @Override
    @Transactional
    public TrainingProgram updateTrainingProgram(Integer programId, TrainingProgram trainingProgram) {
        TrainingProgram existing = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Training program not found"));

        existing.setProgramName(trainingProgram.getProgramName());
        existing.setDescription(trainingProgram.getDescription());
        existing.setDurationHours(trainingProgram.getDurationHours());
        existing.setSkillCategory(trainingProgram.getSkillCategory());
        existing.setLevel(trainingProgram.getLevel());
        existing.setMaxParticipants(trainingProgram.getMaxParticipants());

        return trainingProgramRepository.save(existing);
    }

    @Override
    public List<TrainingProgram> getAllTrainingPrograms() {
        return trainingProgramRepository.findAll();
    }

    @Override
    public List<TrainingProgram> getTrainingProgramsByStatus(TrainingProgram.TrainingStatus status) {
        return trainingProgramRepository.findByStatus(status);
    }

    @Override
    public Optional<TrainingProgram> getTrainingProgramById(Integer programId) {
        return trainingProgramRepository.findById(programId);
    }

    @Override
    public List<TrainingProgram> getTrainingProgramsBySkill(String skillCategory) {
        return trainingProgramRepository.findBySkillCategory(skillCategory);
    }

    // === Training Assignment ===

    @Override
    @Transactional
    public TrainingAssignment assignTraining(Integer empId, Integer programId, Integer assignedBy, String objective) {
        // Check if program exists
        TrainingProgram program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Training program not found"));

        TrainingAssignment assignment = new TrainingAssignment();
        assignment.setEmpId(empId);
        assignment.setProgramId(programId);
        assignment.setProgramName(program.getProgramName());
        assignment.setAssignedBy(assignedBy);
        assignment.setObjective(objective);
        assignment.setTrainingType("COURSE");
        assignment.setStatus(AssignmentStatus.PLANNED);
        assignment.setStartDate(LocalDate.now());
        assignment = trainingAssignmentRepository.save(assignment);

        // Auto-create progress record
        TrainingProgress progress = new TrainingProgress();
        progress.setAssignId(assignment.getAssignId());
        progress.setEmpId(empId);
        progress.setProgramId(programId);
        progress.setEnrollmentDate(LocalDate.now());
        progress.setStatus(ProgressStatus.NOT_STARTED);
        progress.setCompletionPercentage(BigDecimal.ZERO);
        progress.setUpdatedAt(LocalDateTime.now());
        trainingProgressRepository.save(progress);

        return assignment;
    }

    @Override
    @Transactional
    public TrainingAssignment assignMentor(Integer empId, Integer mentorId, Integer assignedBy, String objective) {
        // Prevent self-assignment
        if (empId.equals(mentorId)) {
            throw new RuntimeException("Cannot assign employee as their own mentor");
        }

        TrainingAssignment assignment = new TrainingAssignment();
        assignment.setEmpId(empId);
        assignment.setMentorId(mentorId);
        assignment.setAssignedBy(assignedBy);
        assignment.setObjective(objective);
        assignment.setTrainingType("MENTORING");
        assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        assignment.setStartDate(LocalDate.now());

        return trainingAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public TrainingAssignment updateAssignmentStatus(Integer assignId, AssignmentStatus newStatus) {
        TrainingAssignment assignment = trainingAssignmentRepository.findById(assignId)
                .orElseThrow(() -> new RuntimeException("Training assignment not found"));

        assignment.setStatus(newStatus);
        if (newStatus == AssignmentStatus.COMPLETED || newStatus == AssignmentStatus.CANCELLED) {
            assignment.setEndDate(LocalDate.now());
        }

        return trainingAssignmentRepository.save(assignment);
    }

    @Override
    public List<TrainingAssignment> getAssignmentsByEmployee(Integer empId) {
        return trainingAssignmentRepository.findByEmpId(empId);
    }

    @Override
    public List<TrainingAssignment> getAssignmentsByMentor(Integer mentorId) {
        return trainingAssignmentRepository.findByMentorId(mentorId);
    }

    @Override
    public List<TrainingAssignment> getAssignmentsByProgram(Integer programId) {
        return trainingAssignmentRepository.findByProgramId(programId);
    }

    // === Training Progress ===

    @Override
    @Transactional
    public TrainingProgress createProgress(Integer assignId, Integer empId, Integer programId) {
        TrainingProgress progress = new TrainingProgress();
        progress.setAssignId(assignId);
        progress.setEmpId(empId);
        progress.setProgramId(programId);
        progress.setEnrollmentDate(LocalDate.now());
        progress.setStatus(ProgressStatus.NOT_STARTED);
        progress.setCompletionPercentage(BigDecimal.ZERO);
        progress.setUpdatedAt(LocalDateTime.now());

        return trainingProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public TrainingProgress updateProgress(Integer progressId, BigDecimal completionPercentage, ProgressStatus status) {
        TrainingProgress progress = trainingProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Training progress not found"));

        progress.setCompletionPercentage(completionPercentage);
        progress.setStatus(status);
        progress.setUpdatedAt(LocalDateTime.now());

        if (status == ProgressStatus.IN_PROGRESS && progress.getStartDate() == null) {
            progress.setStartDate(LocalDate.now());
        }
        if (status == ProgressStatus.COMPLETED) {
            progress.setCompletionPercentage(new BigDecimal(100));
            progress.setCompletionDate(LocalDate.now());
        }

        return trainingProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public TrainingProgress updateProgressScore(Integer progressId, BigDecimal finalScore, BigDecimal attendanceRate) {
        TrainingProgress progress = trainingProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Training progress not found"));

        progress.setFinalScore(finalScore);
        progress.setAttendanceRate(attendanceRate);
        progress.setUpdatedAt(LocalDateTime.now());

        return trainingProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public TrainingProgress markTrainingAsComplete(Integer progressId) {
        TrainingProgress progress = trainingProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Training progress not found"));

        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            throw new RuntimeException("Training must be IN_PROGRESS to mark as complete");
        }

        // Change status to AWAITING_EVIDENCE
        progress.setStatus(ProgressStatus.AWAITING_EVIDENCE);
        progress.setCompletionPercentage(new BigDecimal(100));
        progress.setUpdatedAt(LocalDateTime.now());

        // TODO: Send notification to employee: "Vui lòng upload chứng chỉ hoàn thành"
        // TODO: Send notification to manager: "Nhân viên X đã hoàn thành khóa học, đang chờ upload chứng chỉ"

        return trainingProgressRepository.save(progress);
    }

    @Override
    public List<TrainingProgress> getProgressByEmployee(Integer empId) {
        return trainingProgressRepository.findByEmpId(empId);
    }

    @Override
    public List<TrainingProgress> getProgressByProgram(Integer programId) {
        return trainingProgressRepository.findByProgramId(programId);
    }

    @Override
    public Optional<TrainingProgress> getProgressByEmployeeAndProgram(Integer empId, Integer programId) {
        return trainingProgressRepository.findByEmpIdAndProgramId(empId, programId);
    }

    // === Training Certificate ===

    @Override
    @Transactional
    public TrainingCertificate uploadCertificate(Integer empId, Integer programId,
                                                 String certificateName, String fileUrl) {
        TrainingCertificate cert = new TrainingCertificate();
        cert.setEmpId(empId);
        cert.setProgramId(programId);
        cert.setCertificateName(certificateName);
        cert.setFileUrl(fileUrl);
        cert.setStatus(CertificateStatus.PENDING_VERIFICATION);
        cert.setUploadedAt(LocalDateTime.now());

        return trainingCertificateRepository.save(cert);
    }

    @Override
    @Transactional
    public TrainingCertificate verifyCertificate(Integer certId, Boolean isValid,
                                                 Integer verifiedBy, String verificationNote) {
        TrainingCertificate cert = trainingCertificateRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        cert.setVerifiedBy(verifiedBy);
        cert.setVerifiedAt(LocalDateTime.now());
        cert.setVerificationNote(verificationNote);
        cert.setStatus(isValid ? CertificateStatus.VERIFIED : CertificateStatus.REJECTED);

        // Auto-complete progress if verified
        if (isValid && cert.getProgramId() != null) {
            Optional<TrainingProgress> progressOpt =
                    trainingProgressRepository.findByEmpIdAndProgramId(cert.getEmpId(), cert.getProgramId());
            if (progressOpt.isPresent()) {
                TrainingProgress progress = progressOpt.get();
                progress.setStatus(ProgressStatus.COMPLETED);
                progress.setCompletionPercentage(new BigDecimal(100));
                progress.setCompletionDate(LocalDate.now());
                trainingProgressRepository.save(progress);
            }
        }

        return trainingCertificateRepository.save(cert);
    }

    @Override
    public List<TrainingCertificate> getCertificatesByEmployee(Integer empId) {
        return trainingCertificateRepository.findByEmpId(empId);
    }

    @Override
    public List<TrainingCertificate> getCertificatesByProgram(Integer programId) {
        return trainingCertificateRepository.findByProgramId(programId);
    }

    @Override
    public List<TrainingCertificate> getPendingCertificates() {
        return trainingCertificateRepository.findByStatusOrderByUploadedAtDesc(
                CertificateStatus.PENDING_VERIFICATION);
    }

    // === Training Recommendation ===

    @Override
    @Transactional
    public TrainingRecommendation createRecommendation(Integer empId, Integer evalId, Integer programId,
                                                       String reason, String priority, Integer recommendedBy) {
        TrainingRecommendation rec = new TrainingRecommendation();
        rec.setEmpId(empId);
        rec.setEvalId(evalId);
        rec.setProgramId(programId);
        rec.setReason(reason);
        rec.setPriority(priority);
        rec.setStatus(RecommendationStatus.PENDING);
        rec.setRecommendedBy(recommendedBy);
        rec.setRecommendedAt(LocalDateTime.now());

        return trainingRecommendationRepository.save(rec);
    }

    @Override
    @Transactional
    public TrainingRecommendation updateRecommendationStatus(Integer recommendationId,
                                                             RecommendationStatus newStatus) {
        TrainingRecommendation rec = trainingRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recommendation not found"));

        rec.setStatus(newStatus);
        return trainingRecommendationRepository.save(rec);
    }

    @Override
    public List<TrainingRecommendation> getRecommendationsByEmployee(Integer empId) {
        return trainingRecommendationRepository.findByEmpId(empId);
    }
}