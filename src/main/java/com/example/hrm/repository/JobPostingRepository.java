package com.example.hrm.repository;

import com.example.hrm.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository
        extends JpaRepository<JobPosting, Integer> {

    // ================= HR VIEW =================

    List<JobPosting> findByStatus(String status);

    boolean existsBySlug(String slug);


    // ================= PUBLIC BASIC =================

    List<JobPosting> findByIsPublicTrueAndStatusAndExpiryDateAfter(
            String status,
            LocalDate today
    );

    Optional<JobPosting> findBySlugAndIsPublicTrue(String slug);


    // ================= PUBLIC SEARCH + PAGINATION =================

    @Query("""
        SELECT j
        FROM JobPosting j
        WHERE j.isPublic = true
        AND j.status = 'OPEN'
        AND j.expiryDate >= :today
        AND (
            :keyword IS NULL
            OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :location IS NULL
            OR j.location = :location
        )
        AND (
            :department IS NULL
            OR j.recruitmentRequest.department.deptId = :department
        )
        ORDER BY j.publishDate DESC
    """)
    Page<JobPosting> searchPublicJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("department") Integer department,
            @Param("today") LocalDate today,
            Pageable pageable
    );


    // ================= DISTINCT LOCATION =================

    @Query("""
        SELECT DISTINCT j.location
        FROM JobPosting j
        WHERE j.isPublic = true
        AND j.status = 'OPEN'
        AND j.expiryDate >= :today
        ORDER BY j.location
    """)
    List<String> findDistinctPublicLocations(
            @Param("today") LocalDate today
    );


    // ================= COUNT JOB BY LOCATION =================

    @Query("""
        SELECT j.location, COUNT(j)
        FROM JobPosting j
        WHERE j.isPublic = true
        AND j.status = 'OPEN'
        AND j.expiryDate >= :today
        GROUP BY j.location
    """)
    List<Object[]> countJobsByLocation(
            @Param("today") LocalDate today
    );

}