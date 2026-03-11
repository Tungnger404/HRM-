package com.example.hrm.repository;

import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    // =====================================================
    // ================= HR SEARCH + PAGINATION ============
    // =====================================================

    @Query("""
        SELECT c FROM Candidate c
        WHERE c.jobPosting.postingId = :postingId
        AND c.status <> com.example.hrm.entity.CandidateStatus.REJECTED
        AND (:status IS NULL OR c.status = :status)
        AND (
            :keyword IS NULL OR :keyword = '' OR
            LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY c.appliedAt DESC
    """)
    Page<Candidate> searchCandidates(
            @Param("postingId") Integer postingId,
            @Param("status") CandidateStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // =====================================================
    // ================= BASIC ==============================
    // =====================================================

    List<Candidate> findByJobPosting_PostingId(Integer postingId);

    boolean existsByEmailAndJobPosting_PostingId(String email, Integer postingId);

    long countByJobPosting_PostingId(Integer postingId);

    // =====================================================
    // ================= DASHBOARD SUPPORT =================
    // =====================================================

    long countByJobPosting_PostingIdAndStatus(
            Integer postingId,
            CandidateStatus status
    );

}