package com.example.hrm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_documents")
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Integer id;

    // ✅ dùng emp_id (đúng cột chuẩn)
    @Column(name = "emp_id", nullable = false)
    private Integer employeeId;

    // ✅ DB có cả doc_name và title → ta dùng doc_name chính thức
    @Column(name = "doc_name", length = 100)
    private String title;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    // ✅ version đã có trong DB
    @Column(name = "doc_version", nullable = false)
    private Integer version = 1;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "stored_path", nullable = false, length = 255)
    private String storedPath;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "uploaded_at", insertable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // ✅ bỏ @Transient vì DB đã có cột này
    @Column(name = "uploaded_by_user_id")
    private Integer uploadedByUserId;

    // ================= GETTER / SETTER =================

    public Integer getId() { return id; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Integer getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(Integer uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
}