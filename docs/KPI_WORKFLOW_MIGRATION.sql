-- =====================================================
-- DATABASE MIGRATION FOR KPI WORKFLOW ENHANCEMENT
-- =====================================================
-- This script adds support for the complete KPI workflow:
-- HR → Employee → HR Verify → Manager Review
-- =====================================================

-- 1. Update kpi_assignments table for new workflow
ALTER TABLE kpi_assignments
ADD hr_excel_template_path NVARCHAR(255) NULL,
    hr_comment NVARCHAR(MAX) NULL,
    employee_excel_path NVARCHAR(255) NULL,
    employee_comment NVARCHAR(MAX) NULL,
    status NVARCHAR(30) DEFAULT 'ASSIGNED',
    employee_submitted_at DATETIME NULL,
    hr_verified_at DATETIME NULL,
    hr_verified_by INT NULL,
    hr_verification_note NVARCHAR(MAX) NULL;

-- 2. Create notifications table
CREATE TABLE notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    emp_id INT NOT NULL,
    type NVARCHAR(50) NOT NULL,
    title NVARCHAR(200) NOT NULL,
    message NVARCHAR(MAX) NULL,
    link_url NVARCHAR(255) NULL,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
);

-- Create index for faster notification queries
CREATE INDEX idx_notifications_emp_id ON notifications(emp_id);
CREATE INDEX idx_notifications_is_read ON notifications(emp_id, is_read);

-- 3. Create kpi_evidences table
CREATE TABLE kpi_evidences (
    evidence_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    stored_path NVARCHAR(255) NOT NULL,
    content_type NVARCHAR(100) NULL,
    file_size BIGINT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (assignment_id) REFERENCES kpi_assignments(assignment_id) ON DELETE CASCADE
);

-- Create index for faster evidence queries
CREATE INDEX idx_kpi_evidences_assignment_id ON kpi_evidences(assignment_id);

-- 4. Update training_programs table for mentoring and online courses
ALTER TABLE training_programs
ADD training_method NVARCHAR(50) NULL,
    course_url NVARCHAR(500) NULL,
    default_mentor_id INT NULL;

-- 5. Create index for KPI assignment status filtering
CREATE INDEX idx_kpi_assignments_status ON kpi_assignments(status, employee_submitted_at);

-- =====================================================
-- SAMPLE DATA FOR TESTING (Optional - remove if not needed)
-- =====================================================

-- Sample KPI Assignment with HR template
-- INSERT INTO kpi_assignments (cycle_id, emp_id, hr_excel_template_path, hr_comment, status, assigned_at)
-- VALUES (1, 1, '/uploads/kpi_template_2026_Q1.xlsx', 'Vui lòng hoàn thành trước 31/03/2026', 'ASSIGNED', GETDATE());

-- Sample notification
-- INSERT INTO notifications (emp_id, type, title, message, link_url, is_read)
-- VALUES (1, 'KPI_ASSIGNED', 'Bạn có KPI mới cần hoàn thành', 'Vui lòng hoàn thành trước 31/03/2026', '/evaluation/submit-kpi', 0);
