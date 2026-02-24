package com.example.hrm.repository;

import com.example.hrm.entity.KpiEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiEvidenceRepository extends JpaRepository<KpiEvidence, Integer> {
    
    List<KpiEvidence> findByAssignmentIdOrderByUploadedAtDesc(Integer assignmentId);
    
    void deleteByAssignmentId(Integer assignmentId);
}
