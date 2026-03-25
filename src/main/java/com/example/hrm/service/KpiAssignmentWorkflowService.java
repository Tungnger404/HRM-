package com.example.hrm.service;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.repository.KpiAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KpiAssignmentWorkflowService {

    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final NotificationService notificationService;
    private final TrainingService trainingService;
    private final EvaluationPolicyService policyService;
    private final PerformanceRankingService performanceRankingService;

    @Transactional
    public void managerApprove(
            KpiAssignment assignment,
            Integer managerId,
            Integer managerScore,
            String managerComment,
            boolean recommendPromotion,
            boolean recommendTraining,
            String trainingRecommendation) {

        assignment.setManagerScore(managerScore);
        assignment.setManagerComment(managerComment);
        assignment.setManagerReviewedAt(LocalDateTime.now());

        assignment.setPromotionRecommendation(recommendPromotion);
        
        if (recommendTraining && trainingRecommendation != null && !trainingRecommendation.trim().isEmpty()) {
            assignment.setTrainingRecommendation(trainingRecommendation);
        } else {
            assignment.setTrainingRecommendation(null);
        }

        String classification = policyService.calculateClassification(managerScore);
        assignment.setClassification(classification);

        assignment.setStatus(KpiAssignment.AssignmentStatus.COMPLETED);
        kpiAssignmentRepository.save(assignment);

        // Recompute ranking and promotion eligibility from the same manager-scored source.
        performanceRankingService.calculateRankingsForCycle(assignment.getCycleId());
        performanceRankingService.markPromotionEligibility(assignment.getCycleId());

        notificationService.createEvaluationCompletedNotification(
                assignment.getEmpId(),
                assignment.getAssignmentId(),
                managerScore + "/100"
        );

        if (recommendTraining && trainingRecommendation != null && !trainingRecommendation.trim().isEmpty()) {
            trainingService.createRecommendation(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    null,
                    trainingRecommendation,
                    managerScore < 70 ? "HIGH" : "MEDIUM",
                    managerId
            );
        }
    }
}
