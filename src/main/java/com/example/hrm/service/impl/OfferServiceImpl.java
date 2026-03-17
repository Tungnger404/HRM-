package com.example.hrm.service.impl;

import com.example.hrm.entity.*;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.OfferRepository;
import com.example.hrm.service.EmailService;
import com.example.hrm.service.EmployeeService;
import com.example.hrm.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final CandidateRepository candidateRepository;
    private final EmployeeService employeeService;
    private final EmailService emailService;

    @Override
    public void createOffer(Integer candidateId,
                            Double salary,
                            LocalDate startDate,
                            String probation) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (offerRepository.existsByCandidate(candidate)) {
            throw new RuntimeException("Candidate already has an offer");
        }

        RecruitmentRequest request = candidate.getJobPosting().getRecruitmentRequest();

        Offer offer = Offer.builder()
                .candidate(candidate)
                .salaryOffered(salary)
                .startDate(startDate)
                .probationPeriod(probation)
                .status(OfferStatus.DRAFT)
                // GÁN GIÁ TRỊ VÀO ĐÂY ĐỂ HẾT NULL
                .jobId(request.getJobPosition().getJobId())
                .deptId(request.getDepartment().getDeptId())
                .build();

        offerRepository.save(offer);

        candidate.setStatus(CandidateStatus.OFFERED);
        candidateRepository.save(candidate);
    }

    @Override
    public void sendOffer(Integer offerId) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT offer can be sent");
        }

        String acceptLink = "http://localhost:8080/offer/accept/" + offerId;
        String rejectLink = "http://localhost:8080/offer/reject/" + offerId;

        emailService.sendOfferMail(
                offer.getCandidate().getEmail(),
                offer.getCandidate().getFullName(),
                offer.getCandidate().getJobPosting().getTitle(),
                offer.getSalaryOffered(),
                offer.getStartDate(),
                offer.getProbationPeriod(),
                acceptLink,
                rejectLink
        );

        offer.setStatus(OfferStatus.SENT);
        offerRepository.save(offer);
    }


    @Override
    @Transactional
    public void acceptOffer(Integer offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        if (offer.getStatus() == OfferStatus.ACCEPTED) {
            return;
        }
        if (offer.getStatus() == OfferStatus.REJECTED) {
            throw new RuntimeException("Offer was already rejected.");
        }

        if (offer.getStatus() != OfferStatus.SENT) {
            throw new RuntimeException("Offer must be SENT before ACCEPT. Current status = " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);
        Candidate candidate = offer.getCandidate();
        candidate.setStatus(CandidateStatus.OFFER_ACCEPTED);
        candidateRepository.save(candidate);
    }

    @Override
    @Transactional
    public void rejectOffer(Integer offerId) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));


        if (offer.getStatus() == OfferStatus.ACCEPTED
                || offer.getStatus() == OfferStatus.REJECTED) {
            throw new RuntimeException("Offer already finalized.");
        }
        if (offer.getStatus() != OfferStatus.SENT) {
            throw new RuntimeException("Offer must be SENT before REJECT");
        }

        offer.setStatus(OfferStatus.REJECTED);

        Candidate candidate = offer.getCandidate();
        candidate.setStatus(CandidateStatus.REJECTED);

        offerRepository.save(offer);
        candidateRepository.save(candidate);
    }

    @Override
    public Offer findById(Integer id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    @Override
    public void save(Offer offer) {
        offerRepository.save(offer);
    }
}