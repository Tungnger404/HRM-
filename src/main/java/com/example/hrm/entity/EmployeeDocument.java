package com.example.hrm.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_documents")
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id") // ✅ PK thật trong DB
    private Integer id;

    // DB có emp_id NOT NULL (còn employee_id là cột thừa/đã phát sinh)
    @Column(name = "emp_id", nullable = false)
    private Integer employeeId;

    // "title" của người khác -> map vào doc_name (DB đang có)
    @Column(name = "doc_name", length = 100)
    private String title;

    // DB có doc_type
    @Column(name = "doc_type", length = 50)
    private String docType;

    // DB có status (NOT NULL)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    // DB có file_name (NOT NULL)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    // DB có content_type
    @Column(name = "content_type", length = 255)
    private String contentType;

    // DB có stored_path (NOT NULL)
    @Column(name = "stored_path", nullable = false, length = 255)
    private String storedPath;

    // DB có file_url (nếu bạn muốn dùng)
    @Column(name = "file_url", length = 255)
    private String fileUrl;

    // DB có uploaded_at default getdate()
    @Column(name = "uploaded_at", insertable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // ✅ Cột uploaded_by_user_id KHÔNG có trong bảng => để transient để Hibernate không đòi tạo thêm cột
    @Transient
    private Integer uploadedByUserId;

    public Integer getId() {
        return id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public Integer getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Integer uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
