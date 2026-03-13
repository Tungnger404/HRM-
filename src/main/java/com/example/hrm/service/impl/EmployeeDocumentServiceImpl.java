package com.example.hrm.service.impl;

import com.example.hrm.entity.DocumentStatus;
import com.example.hrm.entity.DocumentType;
import com.example.hrm.entity.EmployeeDocument;
import com.example.hrm.repository.EmployeeDocumentRepository;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.EmployeeDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeDocumentRepository repo;
    private final DocumentStorageService storage;

    public EmployeeDocumentServiceImpl(EmployeeDocumentRepository repo, DocumentStorageService storage) {
        this.repo = repo;
        this.storage = storage;
    }

    @Override
    public List<EmployeeDocument> search(Integer empId, String docType, String status, String q) {
        return repo.search(empId, docType, status, q);
    }

    @Override
    public List<EmployeeDocument> findByEmployee(Integer empId) {
        return repo.search(empId, null, null, null);
    }

    private String normalizeDocType(String docType) {
        if (docType == null || docType.isBlank()) return DocumentType.DOCUMENT.name();
        String v = docType.trim().toUpperCase();
        if (!v.equals(DocumentType.CONTRACT.name()) && !v.equals(DocumentType.DOCUMENT.name())) {
            return DocumentType.DOCUMENT.name();
        }
        return v;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return DocumentStatus.DRAFT.name();
        String v = status.trim().toUpperCase();
        try {
            DocumentStatus.valueOf(v);
            return v;
        } catch (Exception ex) {
            return DocumentStatus.DRAFT.name();
        }
    }

    private String normalizeTitle(String title, MultipartFile file) {
        String t = (title == null ? "" : title.trim());
        if (!t.isBlank()) return t;
        String fn = file.getOriginalFilename();
        return (fn == null || fn.isBlank()) ? "document" : fn.trim();
    }

    @Override
    @Transactional
    public EmployeeDocument upload(Integer empId, String title, String docType, String status,
                                   MultipartFile file, Integer uploaderUserId) {

        if (empId == null) throw new IllegalArgumentException("empId is required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is required");

        String stored = storage.store(file);

        String normType = normalizeDocType(docType);
        String normStatus = normalizeStatus(status);
        String normTitle = normalizeTitle(title, file);

        Integer maxVer = repo.findMaxVersion(empId, normType, normTitle);
        int nextVer = (maxVer == null ? 0 : maxVer) + 1;

        EmployeeDocument d = new EmployeeDocument();
        d.setEmployeeId(empId);
        d.setTitle(normTitle);
        d.setDocType(normType);
        d.setStatus(normStatus);
        d.setVersion(nextVer);
        d.setFileName(file.getOriginalFilename());
        d.setContentType(file.getContentType());
        d.setStoredPath(stored);
        d.setUploadedByUserId(uploaderUserId);

        return repo.save(d);
    }

    @Override
    @Transactional
    public EmployeeDocument updateMeta(Integer docId, String title, String docType, String status) {
        EmployeeDocument d = get(docId);

        if (title != null) {
            String t = title.trim();
            if (!t.isBlank()) d.setTitle(t);
        }

        if (docType != null) d.setDocType(normalizeDocType(docType));
        if (status != null) d.setStatus(normalizeStatus(status));

        return repo.save(d);
    }

    @Override
    @Transactional
    public void delete(Integer docId) {
        EmployeeDocument d = get(docId);
        repo.delete(d);
        storage.delete(d.getStoredPath());
    }

    @Override
    public EmployeeDocument get(Integer docId) {
        return repo.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));
    }
}