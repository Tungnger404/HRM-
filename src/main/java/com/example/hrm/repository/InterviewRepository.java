package com.example.hrm.repository;

import com.example.hrm.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {


    List<Interview> findByCandidateCandidateId(Integer candidateId);


    Optional<Interview> findByCandidateCandidateIdAndRoundNumber(
            Integer candidateId,
            Integer roundNumber
    );


    boolean existsByCandidateCandidateIdAndRoundNumber(
            Integer candidateId,
            Integer roundNumber
    );

}