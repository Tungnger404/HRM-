package com.example.hrm.repository;

import com.example.hrm.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository
        extends JpaRepository<JobPosting, Integer> {

    // ===== HR VIEW =====
    List<JobPosting> findByStatus(String status);

    // ===== PUBLIC CAREER PAGE =====
    List<JobPosting> findByIsPublicTrueAndStatusAndExpiryDateAfter(
            String status,
            LocalDate today
    );

    // ===== PUBLIC JOB DETAIL =====
    Optional<JobPosting> findBySlugAndIsPublicTrue(String slug);

    // ===== CHECK SLUG DUPLICATE =====
    boolean existsBySlug(String slug);

}