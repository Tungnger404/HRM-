package com.example.hrm.service.impl;

import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.KpiEvidenceRepository;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.KpiEvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KpiEvidenceServiceImpl implements KpiEvidenceService {

    @Autowired
    private KpiEvidenceRepository evidenceRepository;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Override
    public KpiEvidence saveEvidence(Integer assignmentId, MultipartFile file) {
        String storedPath = documentStorageService.store(file);

        KpiEvidence evidence = new KpiEvidence();
        evidence.setAssignmentId(assignmentId);
        evidence.setFileName(file.getOriginalFilename());
        evidence.setStoredPath(storedPath);
        evidence.setContentType(file.getContentType());
        evidence.setFileSize(file.getSize());
        evidence.setUploadedAt(LocalDateTime.now());

        return evidenceRepository.save(evidence);
    }

    @Override
    public List<KpiEvidence> getEvidencesByAssignment(Integer assignmentId) {
        return evidenceRepository.findByAssignmentIdOrderByUploadedAtDesc(assignmentId);
    }

    @Override
    public void deleteEvidence(Integer evidenceId) {
        evidenceRepository.findById(evidenceId).ifPresent(evidence -> {
            documentStorageService.delete(evidence.getStoredPath());
            evidenceRepository.delete(evidence);
        });
    }

    @Override
    @Transactional
    public void deleteAllEvidencesByAssignment(Integer assignmentId) {
        List<KpiEvidence> evidences = evidenceRepository.findByAssignmentIdOrderByUploadedAtDesc(assignmentId);
        evidences.forEach(evidence -> documentStorageService.delete(evidence.getStoredPath()));
        evidenceRepository.deleteByAssignmentId(assignmentId);
    }
}
