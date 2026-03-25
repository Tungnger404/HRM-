package com.example.hrm.service.impl;

import com.example.hrm.entity.*;
import com.example.hrm.entity.TrainingRecommendation.RecommendationStatus;
import com.example.hrm.repository.*;
import com.example.hrm.service.PerformanceRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceRankingServiceImpl implements PerformanceRankingService {

    @Autowired
    private PerformanceRankingRepository performanceRankingRepository;

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    @Autowired
    private KpiTemplateRepository kpiTemplateRepository;

    @Autowired
    private TrainingRecommendationRepository trainingRecommendationRepository;

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;

    // === Ranking Calculation ===

    @Override
    @Transactional
    public void calculateRankingsForCycle(Integer cycleId) {
        
         // Get all completed KPI assignments in this cycle
         List<KpiAssignment> assignments = kpiAssignmentRepository.findByCycleId(cycleId).stream()
                 .filter(a -> a.getStatus() == KpiAssignment.AssignmentStatus.COMPLETED)
                 .collect(Collectors.toList());

         if (assignments.isEmpty()) {
             return;
         }

         // Sort by manager_score DESC
         assignments.sort(Comparator.comparing(KpiAssignment::getManagerScore,
                 Comparator.nullsLast(Comparator.reverseOrder())));

         int totalEmployees = assignments.size();

         // Calculate rank for each employee
         for (int i = 0; i < assignments.size(); i++) {
             KpiAssignment assignment = assignments.get(i);

             // Check if ranking already exists
             PerformanceRanking ranking = performanceRankingRepository
                     .findByCycleIdAndEmpId(cycleId, assignment.getEmpId())
                     .orElse(new PerformanceRanking());

             ranking.setCycleId(cycleId);
             ranking.setEmpId(assignment.getEmpId());
             ranking.setFinalScore(assignment.getManagerScore() != null ? 
                     BigDecimal.valueOf(assignment.getManagerScore()) : BigDecimal.ZERO);
             ranking.setRankOverall(i + 1);
             ranking.setClassification(assignment.getClassification());

             // Calculate percentile
             BigDecimal percentile = BigDecimal.valueOf((totalEmployees - i) * 100.0 / totalEmployees)
                     .setScale(2, RoundingMode.HALF_UP);
             ranking.setPercentile(percentile);

             // Determine if training required (C or D)
             boolean needTraining = "C".equals(assignment.getClassification()) || "D".equals(assignment.getClassification());
             ranking.setIsTrainingRequired(needTraining);

             if (ranking.getCreatedAt() == null) {
                 ranking.setCreatedAt(LocalDateTime.now());
             }
 
             performanceRankingRepository.save(ranking);
         }
     }

    @Override
    @Transactional
    public void markPromotionEligibility(Integer cycleId) {
        List<PerformanceRanking> rankings = performanceRankingRepository.findByCycleId(cycleId);

        for (PerformanceRanking ranking : rankings) {
            boolean isEligible = false;
            String recommendation = "";

            // Rule 1: Classification A and top 10%
            if ("A".equals(ranking.getClassification()) &&
                    ranking.getPercentile() != null &&
                    ranking.getPercentile().compareTo(new BigDecimal("90")) >= 0) {
                isEligible = true;
                recommendation = "Xuất sắc, nằm trong top 10%. Đề xuất thăng chức.";
            }
            // Rule 2: Classification A and rank in top 5
            else if ("A".equals(ranking.getClassification()) &&
                    ranking.getRankOverall() != null &&
                    ranking.getRankOverall() <= 5) {
                isEligible = true;
                recommendation = "Xuất sắc, nằm trong top 5 nhân viên. Đề xuất thăng chức.";
            }

            ranking.setIsPromotionEligible(isEligible);
            ranking.setRewardRecommendation(recommendation);
            performanceRankingRepository.save(ranking);
        }
    }

    @Override
    public List<PerformanceRanking> getTopPerformers(Integer cycleId, Integer limit) {
        List<PerformanceRanking> rankings = performanceRankingRepository.findByCycleId(cycleId);
        return rankings.stream()
                .sorted(Comparator.comparing(PerformanceRanking::getRankOverall,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceRanking> getPromotionCandidates(Integer cycleId) {
        return performanceRankingRepository.findByIsPromotionEligibleTrue().stream()
                .filter(r -> r.getCycleId().equals(cycleId))
                .collect(Collectors.toList());
    }

    @Override
    public PerformanceRanking getRankingByEmployeeAndCycle(Integer empId, Integer cycleId) {
        return performanceRankingRepository.findByCycleIdAndEmpId(cycleId, empId)
                .orElse(null);
    }

    // === Auto Training Recommendation ===

    @Override
    @Transactional
    public List<TrainingRecommendation> autoCreateTrainingRecommendations(Integer assignmentId) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("KPI Assignment not found"));

        String classification = assignment.getClassification();
        List<TrainingRecommendation> recommendations = new ArrayList<>();

        switch (classification) {
            case "D":
            case "C":
                // Nhân viên yếu - tạo recommendation priority HIGH
                recommendations = analyzeWeakKPIsAndRecommend(assignmentId);
                for (TrainingRecommendation rec : recommendations) {
                    rec.setPriority("HIGH");
                    rec.setReason(rec.getReason() + " [ĐÁNH GIÁ YẾU - CẦN ĐÀO TẠO BẮT BUỘC]");
                }
                break;

            case "B":
                // Nhân viên trung bình - tạo recommendation priority MEDIUM
                recommendations = analyzeWeakKPIsAndRecommend(assignmentId);
                for (TrainingRecommendation rec : recommendations) {
                    rec.setPriority("MEDIUM");
                }
                break;

            case "A":
                // Nhân viên xuất sắc - không tạo recommendation
                // Họ có thể tự đăng ký nếu muốn
                break;

            default:
                break;
        }

        return recommendations;
    }

    @Override
    @Transactional
    public List<TrainingRecommendation> analyzeWeakKPIsAndRecommend(Integer assignmentId) {
        List<TrainingRecommendation> recommendations = new ArrayList<>();

        // Get KPI assignment
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("KPI Assignment not found"));

        Integer empId = assignment.getEmpId();

        // Since KpiAssignment doesn't have detail KPI breakdown in this system,
        // we'll recommend general improvement programs based on classification
        
        if ("C".equals(assignment.getClassification()) || "D".equals(assignment.getClassification())) {
            // Recommend general improvement courses
            List<TrainingProgram> generalPrograms = trainingProgramRepository
                    .findByStatus(TrainingProgram.TrainingStatus.ACTIVE).stream()
                    .limit(3)
                    .collect(Collectors.toList());

            for (TrainingProgram program : generalPrograms) {
                String reason = String.format(
                        "Nhân viên đạt classification %s (score: %d). Cần cải thiện kỹ năng: %s",
                        assignment.getClassification(),
                        assignment.getManagerScore() != null ? assignment.getManagerScore() : 0,
                        program.getSkillCategory()
                );

                TrainingRecommendation rec = createRecommendation(
                        empId,
                        null,
                        program.getProgramId(),
                        reason,
                        "MEDIUM",
                        null // system auto
                );
                recommendations.add(rec);
            }
        }

        return recommendations;
    }

    // Helper method
    private TrainingRecommendation createRecommendation(Integer empId, Integer evalId,
                                                       Integer programId, String reason,
                                                       String priority, Integer recommendedBy) {
        TrainingRecommendation rec = new TrainingRecommendation();
        rec.setEmpId(empId);
        rec.setEvalId(evalId); // May be null - from KpiAssignment instead
        rec.setProgramId(programId);
        rec.setReason(reason);
        rec.setPriority(priority);
        rec.setStatus(RecommendationStatus.RECOMMENDED);
        rec.setRecommendedBy(recommendedBy); // null = system
        rec.setRecommendedAt(LocalDateTime.now());

        return trainingRecommendationRepository.save(rec);
    }
}
