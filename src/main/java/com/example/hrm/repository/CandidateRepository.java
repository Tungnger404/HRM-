package com.example.hrm.repository;

import com.example.hrm.entity.Candidate;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    // ===== SEARCH FOR HR SCREENING =====
    @Query("""
        SELECT c FROM Candidate c
        WHERE c.jobPosting.postingId = :postingId
        AND (:status IS NULL OR c.status = :status)
        AND (:keyword IS NULL OR 
             LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<Candidate> searchCandidates(
            @Param("postingId") Integer postingId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    // ===== LIST ALL BY POSTING =====
    List<Candidate> findByJobPosting_PostingId(Integer postingId);

    // ===== CHECK DUPLICATE EMAIL APPLY =====
    boolean existsByEmailAndJobPosting_PostingId(String email, Integer postingId);

    // ✅ ADD THIS
    long countByJobPosting_PostingId(Integer postingId);
}