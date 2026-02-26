package com.example.hrm.repository;

import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Integer> {
    boolean existsByCandidate(Candidate candidate);
    List<Offer> findByCandidate_FullNameContainingIgnoreCase(String name);
}
