package com.example.hrm.service;

import com.example.hrm.entity.Offer;

import java.time.LocalDate;

public interface OfferService {

    void createOffer(Integer candidateId,
                     Double salary,
                     LocalDate startDate,
                     String probation);

    void sendOffer(Integer offerId);

    void acceptOffer(Integer offerId);

    void rejectOffer(Integer offerId);
    Offer findById(Integer id);
    void save(Offer offer);
}