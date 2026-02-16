package com.example.hrm.service;

import com.example.hrm.entity.EmployeeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeDocumentService {
    List<EmployeeDocument> search(Integer empId, String docType, String status, String q);

    EmployeeDocument upload(Integer empId,
                            String title,
                            String docType,
                            String status,
                            MultipartFile file);

    EmployeeDocument updateMeta(Integer docId, String title, String docType, String status);

    void delete(Integer docId);

    EmployeeDocument get(Integer docId);
}
