package com.example.hrm.service.impl;

import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;

    @Override
    @Transactional
    public void submitEvaluation(Integer candidateId,
                                 Integer roundNumber,
                                 Integer score,
                                 String feedback,
                                 InterviewResult result) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (candidate.getStatus() != CandidateStatus.INTERVIEW_SCHEDULED
                && candidate.getStatus() != CandidateStatus.INTERVIEWED) {
            throw new RuntimeException("Ứng viên hiện không trong trạng thái có thể đánh giá.");
        }
        Interview interview = interviewRepository
                .findByCandidateCandidateIdAndRoundNumber(candidateId, roundNumber)
                .orElseThrow(() -> new RuntimeException("Interview round " + roundNumber + " not found"));
        if (interview.getResult() != null) {
            throw new RuntimeException("Vòng phỏng vấn này đã được chấm điểm trước đó.");
        }


        if (roundNumber == 2) {
            Interview hrInterview = interviewRepository
                    .findByCandidateCandidateIdAndRoundNumber(candidateId, 1)
                    .orElseThrow(() -> new RuntimeException("HR interview not found"));

            if (hrInterview.getResult() != InterviewResult.PASS) {
                throw new RuntimeException("HR has not passed this candidate yet.");
            }
        }

        InterviewResult finalResult = (score >= 5) ? InterviewResult.PASS : InterviewResult.FAIL;
        interview.setScore(score);
        interview.setFeedback(feedback);
        interview.setResult(finalResult);
        interviewRepository.save(interview);


        if (finalResult == InterviewResult.FAIL) {
            candidate.setStatus(CandidateStatus.REJECTED);
        } else {
            if (roundNumber == 1) {
                candidate.setStatus(CandidateStatus.INTERVIEWED);
                boolean round2Exists = interviewRepository
                        .existsByCandidateCandidateIdAndRoundNumber(candidateId, 2);

                if (!round2Exists) {
                    Interview managerInterview = new Interview();
                    managerInterview.setCandidate(candidate);
                    managerInterview.setRoundNumber(2);
                    managerInterview.setScheduledTime(LocalDateTime.now().plusDays(2));
                    managerInterview.setLocation("Manager Office");
                    interviewRepository.save(managerInterview);
                }
            } else if (roundNumber == 2) {
                candidate.setStatus(CandidateStatus.OFFERED);
            }
        }
        candidateRepository.save(candidate);
    }
    @Override
    public void scheduleInterview(Integer candidateId,
                                  Integer roundNumber,
                                  LocalDateTime time,
                                  String location) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));


        if (interviewRepository.existsByCandidateCandidateIdAndRoundNumber(candidateId, roundNumber)) {
            throw new RuntimeException("Interview round already exists");
        }

        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setRoundNumber(roundNumber);
        interview.setScheduledTime(time);
        interview.setLocation(location);

        interviewRepository.save(interview);

        candidate.setStatus(CandidateStatus.INTERVIEW_SCHEDULED);
        candidateRepository.save(candidate);
    }
}