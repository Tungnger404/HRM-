package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_evidences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Integer evidenceId;

    // Foreign key to kpi_assignments
    @Column(name = "assignment_id", nullable = false)
    private Integer assignmentId;

    // Quan hệ tới KpiAssignment (mỗi evidence thuộc 1 assignment)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false, insertable = false, updatable = false)
    private KpiAssignment assignment;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;          // tên file user upload (TestingSWP.xlsx)

    @Column(name = "content_type", length = 100)
    private String contentType;       // ví dụ: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

    @Column(name = "file_size")
    private Long fileSize;            // kích thước (bytes)

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;        // đường dẫn file lưu trên server

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt; // thời điểm lưu
}