package com.example.hrm.repository;

import com.example.hrm.entity.JobDescription;
import com.example.hrm.entity.JobDescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Integer> {

    List<JobDescription> findByStatus(JobDescriptionStatus status);
    @Query("""
           SELECT jd FROM JobDescription jd
           JOIN FETCH jd.job
           WHERE jd.id = :id
           """)
    Optional<JobDescription> findByIdWithJob(@Param("id") Integer id);
    boolean existsByRecruitmentRequest_ReqId(Integer reqId);
}

