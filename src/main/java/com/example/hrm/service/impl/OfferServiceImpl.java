package com.example.hrm.service.impl;

import com.example.hrm.entity.*;
import com.example.hrm.entity.OfferStatus;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.OfferRepository;
import com.example.hrm.service.EmployeeService;
import com.example.hrm.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final CandidateRepository candidateRepository;
    private final EmployeeService employeeService;

    @Override
    public void createOffer(Integer candidateId,
                            Double salary,
                            LocalDate startDate,
                            String probation) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        Offer offer = Offer.builder()
                .candidate(candidate)
                .salaryOffered(salary)
                .startDate(startDate)
                .probationPeriod(probation)
                .status(OfferStatus.DRAFT)
                .build();

        offerRepository.save(offer);

        candidate.setStatus(CandidateStatus.OFFERED);
        candidateRepository.save(candidate);
    }

    @Override
    public void sendOffer(Integer offerId) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        offer.setStatus(OfferStatus.SENT);
        offerRepository.save(offer);
    }

    @Override
    public void acceptOffer(Integer offerId) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        offer.setStatus(OfferStatus.ACCEPTED);

        Candidate candidate = offer.getCandidate();
        candidate.setStatus(CandidateStatus.HIRED);

        offerRepository.save(offer);
        candidateRepository.save(candidate);

        employeeService.createFromCandidate(
                candidate,
                offer.getStartDate()
        );
    }

    @Override
    public void rejectOffer(Integer offerId) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        offer.setStatus(OfferStatus.REJECTED);
        offerRepository.save(offer);
    }
    @Override
    public Offer findById(Integer id) {
        return offerRepository.findById(id).orElseThrow();
    }

    @Override
    public void save(Offer offer) {
        offerRepository.save(offer);
    }
}