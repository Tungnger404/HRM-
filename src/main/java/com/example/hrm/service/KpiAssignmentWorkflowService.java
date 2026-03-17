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

        System.out.println("DEBUG: managerApprove called - assignmentId: " + assignment.getAssignmentId() 
            + ", cycleId: " + assignment.getCycleId() + ", empId: " + assignment.getEmpId()
            + ", managerScore: " + managerScore);

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

        System.out.println("DEBUG: Assignment saved - status set to COMPLETED");

        // Auto-calculate performance ranking for this cycle
        System.out.println("DEBUG: Calling calculateRankingsForCycle with cycleId: " + assignment.getCycleId());
        performanceRankingService.calculateRankingsForCycle(assignment.getCycleId());
        System.out.println("DEBUG: calculateRankingsForCycle completed");

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

            notificationService.createTrainingRecommendationNotification(
                    assignment.getEmpId(),
                    trainingRecommendation
            );
        }
    }
}
