package com.example.hrm.evaluation.service.impl;

import com.example.hrm.evaluation.model.*;
import com.example.hrm.evaluation.model.TrainingRecommendation.RecommendationStatus;
import com.example.hrm.evaluation.repository.*;
import com.example.hrm.evaluation.service.PerformanceRankingService;
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
    private EvaluationRepository evaluationRepository;

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
        // Get all completed evaluations in this cycle
        List<Evaluation> evaluations = evaluationRepository.findByCycleIdAndStatus(
                cycleId, Evaluation.EvaluationStatus.COMPLETED);

        if (evaluations.isEmpty()) {
            return;
        }

        // Sort by final_score DESC
        evaluations.sort(Comparator.comparing(Evaluation::getFinalScore,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int totalEmployees = evaluations.size();

        // Calculate rank for each employee
        for (int i = 0; i < evaluations.size(); i++) {
            Evaluation eval = evaluations.get(i);

            // Check if ranking already exists
            PerformanceRanking ranking = performanceRankingRepository
                    .findByCycleIdAndEmpId(cycleId, eval.getEmpId())
                    .orElse(new PerformanceRanking());

            ranking.setCycleId(cycleId);
            ranking.setEmpId(eval.getEmpId());
            ranking.setFinalScore(eval.getFinalScore());
            ranking.setRankOverall(i + 1);
            ranking.setClassification(eval.getClassification());

            // Calculate percentile
            BigDecimal percentile = BigDecimal.valueOf((totalEmployees - i) * 100.0 / totalEmployees)
                    .setScale(2, RoundingMode.HALF_UP);
            ranking.setPercentile(percentile);

            // Determine if training required (C or D)
            boolean needTraining = "C".equals(eval.getClassification()) || "D".equals(eval.getClassification());
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
            // Rule 3: Check history - 2 consecutive cycles with A/B (TODO: implement later)

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
    public List<TrainingRecommendation> autoCreateTrainingRecommendations(Integer evalId) {
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        String classification = evaluation.getClassification();
        List<TrainingRecommendation> recommendations = new ArrayList<>();

        switch (classification) {
            case "D":
            case "C":
                // Nhân viên yếu - tạo recommendation priority HIGH
                recommendations = analyzeWeakKPIsAndRecommend(evalId);
                for (TrainingRecommendation rec : recommendations) {
                    rec.setPriority("HIGH");
                    rec.setReason(rec.getReason() + " [ĐÁNH GIÁ YẾU - CẦN ĐÀO TẠO BẮT BUỘC]");
                }
                // TODO: Send notification to Manager
                break;

            case "B":
                // Nhân viên trung bình - tạo recommendation priority MEDIUM
                recommendations = analyzeWeakKPIsAndRecommend(evalId);
                for (TrainingRecommendation rec : recommendations) {
                    rec.setPriority("MEDIUM");
                }
                // TODO: Send notification to Employee
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
    public List<TrainingRecommendation> analyzeWeakKPIsAndRecommend(Integer evalId) {
        List<TrainingRecommendation> recommendations = new ArrayList<>();

        // Get evaluation
        Evaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        // Find KPIs with finalScore < 60 (weak)
        List<EvaluationDetail> weakKPIs = evaluationDetailRepository.findByEvalId(evalId).stream()
                .filter(detail -> detail.getFinalScore() != null &&
                        detail.getFinalScore().compareTo(new BigDecimal("60")) < 0)
                .collect(Collectors.toList());

        if (weakKPIs.isEmpty()) {
            // No weak KPI, but still classification C/D
            // Recommend general improvement courses
            List<TrainingProgram> generalPrograms = trainingProgramRepository
                    .findByStatus(TrainingProgram.TrainingStatus.ACTIVE).stream()
                    .limit(3)
                    .collect(Collectors.toList());

            for (TrainingProgram program : generalPrograms) {
                TrainingRecommendation rec = createRecommendation(
                        evaluation.getEmpId(),
                        evalId,
                        program.getProgramId(),
                        "Đề xuất cải thiện năng lực tổng thể",
                        "MEDIUM",
                        null // system auto
                );
                recommendations.add(rec);
            }
            return recommendations;
        }

        // For each weak KPI, find matching training programs
        for (EvaluationDetail detail : weakKPIs) {
            KpiTemplate kpi = kpiTemplateRepository.findById(detail.getKpiId())
                    .orElse(null);

            if (kpi == null) continue;

            // Match training programs by skill_category containing KPI name
            List<TrainingProgram> matchedPrograms = trainingProgramRepository
                    .findBySkillCategoryContaining(kpi.getKpiName());

            if (matchedPrograms.isEmpty()) {
                // Try fuzzy match (split by space and search)
                String[] keywords = kpi.getKpiName().split("\\s+");
                for (String keyword : keywords) {
                    matchedPrograms.addAll(trainingProgramRepository
                            .findBySkillCategoryContaining(keyword));
                }
            }

            // Create recommendation for the best matched program
            if (!matchedPrograms.isEmpty()) {
                TrainingProgram program = matchedPrograms.get(0); // Take first match

                String reason = String.format(
                        "KPI '%s' chỉ đạt %.2f điểm (yếu). Cần đào tạo kỹ năng: %s",
                        kpi.getKpiName(),
                        detail.getFinalScore(),
                        program.getSkillCategory()
                );

                TrainingRecommendation rec = createRecommendation(
                        evaluation.getEmpId(),
                        evalId,
                        program.getProgramId(),
                        reason,
                        "HIGH",
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
        rec.setEvalId(evalId);
        rec.setProgramId(programId);
        rec.setReason(reason);
        rec.setPriority(priority);
        rec.setStatus(RecommendationStatus.PENDING);
        rec.setRecommendedBy(recommendedBy); // null = system
        rec.setRecommendedAt(LocalDateTime.now());

        return trainingRecommendationRepository.save(rec);
    }
}
