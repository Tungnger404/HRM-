package com.example.hrm.service.impl;

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
    @Transactional
    public EmployeeDocument upload(Integer empId, String title, String docType, String status, MultipartFile file) {
        if (empId == null) throw new IllegalArgumentException("empId is required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is required");

        String stored = storage.store(file);

        EmployeeDocument d = new EmployeeDocument();
        d.setEmployeeId(empId);
        d.setTitle(title == null ? "" : title.trim());
        d.setDocType(docType == null ? "DOCUMENT" : docType.trim());
        d.setStatus((status == null || status.isBlank()) ? "DRAFT" : status.trim());
        d.setFileName(file.getOriginalFilename());
        d.setContentType(file.getContentType());
        d.setStoredPath(stored);

        return repo.save(d);
    }

    @Override
    @Transactional
    public EmployeeDocument updateMeta(Integer docId, String title, String docType, String status) {
        EmployeeDocument d = get(docId);
        if (title != null) d.setTitle(title.trim());
        if (docType != null && !docType.isBlank()) d.setDocType(docType.trim());
        if (status != null && !status.isBlank()) d.setStatus(status.trim());
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
