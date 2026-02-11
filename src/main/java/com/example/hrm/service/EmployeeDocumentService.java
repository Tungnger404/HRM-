package com.example.hrm.service;

import com.example.hrm.entity.DocumentStatus;
import com.example.hrm.entity.DocumentType;
import com.example.hrm.entity.EmployeeDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeDocumentService {
    EmployeeDocument upload(Integer employeeId, String title, DocumentType docType, MultipartFile file, Integer uploaderUserId);
    Resource loadAsResource(Integer docId);
    EmployeeDocument get(Integer docId);

    List<EmployeeDocument> listAll();
    List<EmployeeDocument> listByEmployee(Integer employeeId);

    void updateStatus(Integer docId, DocumentStatus status);
    void delete(Integer docId);
}
