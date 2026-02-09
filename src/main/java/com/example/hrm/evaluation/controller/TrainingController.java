package com.example.hrm.evaluation.controller;

import com.example.hrm.evaluation.model.*;
import com.example.hrm.evaluation.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    // =========================================================
    // Training Program
    // =========================================================

    // POST /api/training/programs
    @PostMapping("/programs")
    public ResponseEntity<TrainingProgram> createTrainingProgram(@RequestBody TrainingProgram trainingProgram) {
        try {
            TrainingProgram created = trainingService.createTrainingProgram(trainingProgram);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/training/programs/{programId}
    @PutMapping("/programs/{programId}")
    public ResponseEntity<TrainingProgram> updateTrainingProgram(@PathVariable Integer programId,
                                                                 @RequestBody TrainingProgram trainingProgram) {
        try {
            TrainingProgram updated = trainingService.updateTrainingProgram(programId, trainingProgram);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // GET /api/training/programs
    @GetMapping("/programs")
    public ResponseEntity<List<TrainingProgram>> getAllTrainingPrograms() {
        return ResponseEntity.ok(trainingService.getAllTrainingPrograms());
    }

    // GET /api/training/programs/status/{status}
    @GetMapping("/programs/status/{status}")
    public ResponseEntity<List<TrainingProgram>> getTrainingProgramsByStatus(@PathVariable String status) {
        try {
            TrainingProgram.TrainingStatus trainingStatus = TrainingProgram.TrainingStatus.valueOf(status);
            return ResponseEntity.ok(trainingService.getTrainingProgramsByStatus(trainingStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/training/programs/{programId}
    @GetMapping("/programs/{programId}")
    public ResponseEntity<TrainingProgram> getTrainingProgramById(@PathVariable Integer programId) {
        Optional<TrainingProgram> program = trainingService.getTrainingProgramById(programId);
        return program.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/training/programs/skill/{skillCategory}
    @GetMapping("/programs/skill/{skillCategory}")
    public ResponseEntity<List<TrainingProgram>> getTrainingProgramsBySkill(@PathVariable String skillCategory) {
        return ResponseEntity.ok(trainingService.getTrainingProgramsBySkill(skillCategory));
    }

    // =========================================================
    // Training Assignment
    // =========================================================

    // POST /api/training/assign  (COURSE or WORKSHOP)
    @PostMapping("/assign")
    public ResponseEntity<TrainingAssignment> assignTraining(@RequestBody Map<String, Object> body) {
        try {
            Integer empId = ((Number) body.get("empId")).intValue();
            Integer programId = ((Number) body.get("programId")).intValue();
            Integer assignedBy = ((Number) body.get("assignedBy")).intValue();
            String objective = (String) body.get("objective");

            TrainingAssignment assignment = trainingService.assignTraining(empId, programId, assignedBy, objective);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // POST /api/training/assign/mentor  (MENTORING)
    @PostMapping("/assign/mentor")
    public ResponseEntity<TrainingAssignment> assignMentor(@RequestBody Map<String, Object> body) {
        try {
            Integer empId = ((Number) body.get("empId")).intValue();
            Integer mentorId = ((Number) body.get("mentorId")).intValue();
            Integer assignedBy = ((Number) body.get("assignedBy")).intValue();
            String objective = (String) body.get("objective");

            TrainingAssignment assignment = trainingService.assignMentor(empId, mentorId, assignedBy, objective);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/training/assignment/{assignId}/status
    @PutMapping("/assignment/{assignId}/status")
    public ResponseEntity<TrainingAssignment> updateAssignmentStatus(@PathVariable Integer assignId,
                                                                     @RequestBody Map<String, String> body) {
        try {
            TrainingAssignment.AssignmentStatus newStatus =
                    TrainingAssignment.AssignmentStatus.valueOf(body.get("status"));
            TrainingAssignment assignment = trainingService.updateAssignmentStatus(assignId, newStatus);
            return ResponseEntity.ok(assignment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/training/assignment/employee/{empId}
    @GetMapping("/assignment/employee/{empId}")
    public ResponseEntity<List<TrainingAssignment>> getAssignmentsByEmployee(@PathVariable Integer empId) {
        return ResponseEntity.ok(trainingService.getAssignmentsByEmployee(empId));
    }

    // GET /api/training/assignment/mentor/{mentorId}
    @GetMapping("/assignment/mentor/{mentorId}")
    public ResponseEntity<List<TrainingAssignment>> getAssignmentsByMentor(@PathVariable Integer mentorId) {
        return ResponseEntity.ok(trainingService.getAssignmentsByMentor(mentorId));
    }

    // GET /api/training/assignment/program/{programId}
    @GetMapping("/assignment/program/{programId}")
    public ResponseEntity<List<TrainingAssignment>> getAssignmentsByProgram(@PathVariable Integer programId) {
        return ResponseEntity.ok(trainingService.getAssignmentsByProgram(programId));
    }

    // =========================================================
    // Training Progress
    // =========================================================

    // PUT /api/training/progress/{progressId}
    @PutMapping("/progress/{progressId}")
    public ResponseEntity<TrainingProgress> updateProgress(@PathVariable Integer progressId,
                                                           @RequestBody Map<String, Object> body) {
        try {
            BigDecimal completionPercentage = new BigDecimal(body.get("completionPercentage").toString());
            TrainingProgress.ProgressStatus status =
                    TrainingProgress.ProgressStatus.valueOf((String) body.get("status"));

            TrainingProgress progress = trainingService.updateProgress(progressId, completionPercentage, status);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/training/progress/{progressId}/score
    @PutMapping("/progress/{progressId}/score")
    public ResponseEntity<TrainingProgress> updateProgressScore(@PathVariable Integer progressId,
                                                                @RequestBody Map<String, Object> body) {
        try {
            BigDecimal finalScore = new BigDecimal(body.get("finalScore").toString());
            BigDecimal attendanceRate = new BigDecimal(body.get("attendanceRate").toString());

            TrainingProgress progress = trainingService.updateProgressScore(progressId, finalScore, attendanceRate);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/training/progress/{progressId}/complete
     * Nhân viên báo đã hoàn thành khóa học
     * Status: IN_PROGRESS -> AWAITING_EVIDENCE
     * Hệ thống yêu cầu upload chứng chỉ
     */
    @PutMapping("/progress/{progressId}/complete")
    public ResponseEntity<TrainingProgress> markTrainingAsComplete(@PathVariable Integer progressId) {
        try {
            TrainingProgress progress = trainingService.markTrainingAsComplete(progressId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/training/progress/employee/{empId}
    @GetMapping("/progress/employee/{empId}")
    public ResponseEntity<List<TrainingProgress>> getProgressByEmployee(@PathVariable Integer empId) {
        return ResponseEntity.ok(trainingService.getProgressByEmployee(empId));
    }

    // GET /api/training/progress/program/{programId}
    @GetMapping("/progress/program/{programId}")
    public ResponseEntity<List<TrainingProgress>> getProgressByProgram(@PathVariable Integer programId) {
        return ResponseEntity.ok(trainingService.getProgressByProgram(programId));
    }

    // GET /api/training/progress/employee/{empId}/program/{programId}
    @GetMapping("/progress/employee/{empId}/program/{programId}")
    public ResponseEntity<TrainingProgress> getProgressByEmployeeAndProgram(@PathVariable Integer empId,
                                                                            @PathVariable Integer programId) {
        Optional<TrainingProgress> progress = trainingService.getProgressByEmployeeAndProgram(empId, programId);
        return progress.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // Training Certificate
    // =========================================================

    // POST /api/training/certificate
    @PostMapping("/certificate")
    public ResponseEntity<TrainingCertificate> uploadCertificate(@RequestBody Map<String, Object> body) {
        try {
            Integer empId = ((Number) body.get("empId")).intValue();
            Integer programId = ((Number) body.get("programId")).intValue();
            String certificateName = (String) body.get("certificateName");
            String fileUrl = (String) body.get("fileUrl");

            TrainingCertificate cert = trainingService.uploadCertificate(empId, programId, certificateName, fileUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(cert);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/training/certificate/{certId}/verify
    @PutMapping("/certificate/{certId}/verify")
    public ResponseEntity<TrainingCertificate> verifyCertificate(@PathVariable Integer certId,
                                                                 @RequestBody Map<String, Object> body) {
        try {
            Boolean isValid = (Boolean) body.get("isValid");
            Integer verifiedBy = ((Number) body.get("verifiedBy")).intValue();
            String verificationNote = (String) body.get("verificationNote");

            TrainingCertificate cert = trainingService.verifyCertificate(certId, isValid, verifiedBy, verificationNote);
            return ResponseEntity.ok(cert);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/training/certificate/employee/{empId}
    @GetMapping("/certificate/employee/{empId}")
    public ResponseEntity<List<TrainingCertificate>> getCertificatesByEmployee(@PathVariable Integer empId) {
        return ResponseEntity.ok(trainingService.getCertificatesByEmployee(empId));
    }

    // GET /api/training/certificate/pending
    @GetMapping("/certificate/pending")
    public ResponseEntity<List<TrainingCertificate>> getPendingCertificates() {
        return ResponseEntity.ok(trainingService.getPendingCertificates());
    }

    // =========================================================
    // Training Recommendation
    // =========================================================

    // POST /api/training/recommendation
    @PostMapping("/recommendation")
    public ResponseEntity<TrainingRecommendation> createRecommendation(@RequestBody Map<String, Object> body) {
        try {
            Integer empId = ((Number) body.get("empId")).intValue();
            Integer evalId = ((Number) body.get("evalId")).intValue();
            Integer programId = ((Number) body.get("programId")).intValue();
            String reason = (String) body.get("reason");
            String priority = (String) body.get("priority");
            Integer recommendedBy = ((Number) body.get("recommendedBy")).intValue();

            TrainingRecommendation rec = trainingService.createRecommendation(
                    empId, evalId, programId, reason, priority, recommendedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(rec);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/training/recommendation/{recommendationId}/status
    @PutMapping("/recommendation/{recommendationId}/status")
    public ResponseEntity<TrainingRecommendation> updateRecommendationStatus(
            @PathVariable Integer recommendationId, @RequestBody Map<String, String> body) {
        try {
            TrainingRecommendation.RecommendationStatus newStatus =
                    TrainingRecommendation.RecommendationStatus.valueOf(body.get("status"));
            TrainingRecommendation rec = trainingService.updateRecommendationStatus(recommendationId, newStatus);
            return ResponseEntity.ok(rec);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/training/recommendation/employee/{empId}
    @GetMapping("/recommendation/employee/{empId}")
    public ResponseEntity<List<TrainingRecommendation>> getRecommendationsByEmployee(@PathVariable Integer empId) {
        return ResponseEntity.ok(trainingService.getRecommendationsByEmployee(empId));
    }
}