package com.example.hrm.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {

    private static final List<String> EXCEL_CONTENT_TYPES = Arrays.asList(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel"
    );

    private static final List<String> EVIDENCE_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final List<String> EXCEL_EXTENSIONS = Arrays.asList(".xlsx", ".xls");
    
    private static final List<String> EVIDENCE_EXTENSIONS = Arrays.asList(
        ".pdf", ".xlsx", ".xls", ".docx", ".doc", ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        
        return EXCEL_CONTENT_TYPES.contains(contentType) && EXCEL_EXTENSIONS.contains(extension);
    }

    public boolean isValidEvidenceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        
        return EVIDENCE_CONTENT_TYPES.contains(contentType) && EVIDENCE_EXTENSIONS.contains(extension);
    }

    public boolean validateFileSize(MultipartFile file, long maxSizeInMB) {
        return file.getSize() <= maxSizeInMB * 1024 * 1024;
    }

    public String validateFiles(MultipartFile excelFile, List<MultipartFile> evidenceFiles) {
        if (excelFile == null || excelFile.isEmpty()) {
            return "Please upload KPI Excel file";
        }
        
        if (!isValidExcelFile(excelFile)) {
            return "Invalid Excel file. Only .xlsx or .xls accepted";
        }
        
        if (!validateFileSize(excelFile, 10)) {
            return "Excel file exceeds 10MB";
        }
        
        if (evidenceFiles == null || evidenceFiles.isEmpty()) {
            return "Please upload at least 1 evidence file";
        }
        
        if (evidenceFiles.size() > 10) {
            return "Maximum 10 evidence files allowed";
        }
        
        for (MultipartFile file : evidenceFiles) {
            if (!file.isEmpty()) {
                if (!isValidEvidenceFile(file)) {
                    return "File " + file.getOriginalFilename() + " not supported. Only accept: PDF, Excel, Word, Images";
                }
                
                if (!validateFileSize(file, 10)) {
                    return "File " + file.getOriginalFilename() + " exceeds 10MB";
                }
            }
        }
        
        long totalSize = excelFile.getSize();
        for (MultipartFile file : evidenceFiles) {
            if (!file.isEmpty()) {
                totalSize += file.getSize();
            }
        }
        
        if (totalSize > 50 * 1024 * 1024) {
            return "Total file size exceeds 50MB";
        }
        
        return null;
    }
}
