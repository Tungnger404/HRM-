package com.example.hrm.controller;

import com.example.hrm.entity.Evaluation;
import com.example.hrm.entity.Evaluation.EvaluationStatus;
import com.example.hrm.entity.EvaluationEvidence;
import com.example.hrm.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    // === Employee ===

    // POST /api/evaluation/create
    @PostMapping("/create")
    public ResponseEntity<Evaluation> createEvaluation(@RequestBody Map<String, Object> body) {
        try {
            Integer empId = ((Number) body.get("empId")).intValue();
            Integer cycleId = ((Number) body.get("cycleId")).intValue();

            Evaluation evaluation = evaluationService.createEvaluation(empId, cycleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(evaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/evaluation/{evalId}/self-score
    @PutMapping("/{evalId}/self-score")
    public ResponseEntity<Void> submitSelfScore(@PathVariable Integer evalId,
                                                @RequestBody Map<String, Object> body) {
        try {
            Integer kpiId = ((Number) body.get("kpiId")).intValue();
            BigDecimal selfScore = new BigDecimal(body.get("selfScore").toString());

            evaluationService.submitSelfScore(evalId, kpiId, selfScore);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/evaluation/{evalId}/submit  (SELF_REVIEW -> MANAGER_REVIEW)
    @PutMapping("/{evalId}/submit")
    public ResponseEntity<Evaluation> submitEvaluation(@PathVariable Integer evalId,
                                                       @RequestBody Map<String, String> body) {
        try {
            Evaluation evaluation = evaluationService.submitEvaluation(evalId, body.get("comment"));
            return ResponseEntity.ok(evaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // GET /api/evaluation/employee/{empId}/history
    @GetMapping("/employee/{empId}/history")
    public ResponseEntity<List<Evaluation>> getEvaluationHistory(@PathVariable Integer empId) {
        return ResponseEntity.ok(evaluationService.getEvaluationHistory(empId));
    }

    // GET /api/evaluation/{evalId}
    @GetMapping("/{evalId}")
    public ResponseEntity<Evaluation> getEvaluationById(@PathVariable Integer evalId) {
        try {
            return ResponseEntity.ok(evaluationService.getEvaluationById(evalId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // === Evidence ===

    // POST /api/evaluation/{evalId}/evidence
    @PostMapping("/{evalId}/evidence")
    public ResponseEntity<EvaluationEvidence> uploadEvidence(@PathVariable Integer evalId,
                                                             @RequestBody Map<String, Object> body) {
        try {
            Integer kpiId = ((Number) body.get("kpiId")).intValue();
            String fileUrl = (String) body.get("fileUrl");
            String description = (String) body.get("description");

            EvaluationEvidence evidence = evaluationService.uploadEvidence(evalId, kpiId, fileUrl, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/evaluation/evidence/{evidenceId}/verify  (HR verifies)
    @PutMapping("/evidence/{evidenceId}/verify")
    public ResponseEntity<EvaluationEvidence> verifyEvidence(@PathVariable Integer evidenceId,
                                                             @RequestBody Map<String, Object> body) {
        try {
            Boolean isValid = (Boolean) body.get("isValid");
            Integer verifiedBy = ((Number) body.get("verifiedBy")).intValue();

            EvaluationEvidence evidence = evaluationService.verifyEvidence(evidenceId, isValid, verifiedBy);
            return ResponseEntity.ok(evidence);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // === Manager ===

    // GET /api/evaluation/manager/{managerId}/pending
    @GetMapping("/manager/{managerId}/pending")
    public ResponseEntity<List<Evaluation>> getManagerPendingEvaluations(@PathVariable Integer managerId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByManagerAndStatus(
                managerId, EvaluationStatus.MANAGER_REVIEW));
    }

    // PUT /api/evaluation/{evalId}/manager-score
    @PutMapping("/{evalId}/manager-score")
    public ResponseEntity<Void> submitManagerScore(@PathVariable Integer evalId,
                                                   @RequestBody Map<String, Object> body) {
        try {
            Integer kpiId = ((Number) body.get("kpiId")).intValue();
            BigDecimal managerScore = new BigDecimal(body.get("managerScore").toString());

            evaluationService.submitManagerScore(evalId, kpiId, managerScore);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/evaluation/{evalId}/approve  (MANAGER_REVIEW -> COMPLETED)
    @PutMapping("/{evalId}/approve")
    public ResponseEntity<Evaluation> approveEvaluation(@PathVariable Integer evalId,
                                                        @RequestBody Map<String, String> body) {
        try {
            Evaluation evaluation = evaluationService.approveEvaluation(evalId, body.get("managerComment"));
            return ResponseEntity.ok(evaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/evaluation/{evalId}/reject  (back to SELF_REVIEW)
    @PutMapping("/{evalId}/reject")
    public ResponseEntity<Evaluation> rejectEvaluation(@PathVariable Integer evalId,
                                                       @RequestBody Map<String, String> body) {
        try {
            Evaluation evaluation = evaluationService.rejectEvaluation(evalId, body.get("managerComment"));
            return ResponseEntity.ok(evaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // === Report ===

    // GET /api/evaluation/employee/{empId}
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<Evaluation>> getEmployeeEvaluations(@PathVariable Integer empId) {
        return ResponseEntity.ok(evaluationService.getEmployeeEvaluations(empId));
    }

    // GET /api/evaluation/cycle/{cycleId}
    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<Evaluation>> getEvaluationsByCycle(@PathVariable Integer cycleId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByCycle(cycleId));
    }
}