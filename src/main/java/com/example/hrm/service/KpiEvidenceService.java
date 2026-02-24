package com.example.hrm.service;

import com.example.hrm.entity.KpiEvidence;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KpiEvidenceService {
    
    KpiEvidence saveEvidence(Integer assignmentId, MultipartFile file);
    
    List<KpiEvidence> getEvidencesByAssignment(Integer assignmentId);
    
    void deleteEvidence(Integer evidenceId);
    
    void deleteAllEvidencesByAssignment(Integer assignmentId);
}
