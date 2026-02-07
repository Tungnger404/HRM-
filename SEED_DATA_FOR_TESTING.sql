-- ================================================================
-- SEED DATA FOR TESTING EVALUATION & TRAINING MODULE
-- ================================================================

USE hrm_system_db;
GO

-- ================================================================
-- 1. INSERT DEPARTMENTS
-- ================================================================
SET IDENTITY_INSERT departments ON;
INSERT INTO departments (dept_id, dept_name, manager_id, parent_dept_id) VALUES
(1, N'IT Department', NULL, NULL),
(2, N'HR Department', NULL, NULL),
(3, N'Sales Department', NULL, NULL);
SET IDENTITY_INSERT departments OFF;
GO

-- ================================================================
-- 2. INSERT JOB POSITIONS
-- ================================================================
SET IDENTITY_INSERT job_positions ON;
INSERT INTO job_positions (job_id, title, job_level, description) VALUES
(1, N'Software Engineer', 2, N'Develop and maintain software'),
(2, N'HR Manager', 3, N'Manage HR operations'),
(3, N'Sales Executive', 2, N'Handle sales activities');
SET IDENTITY_INSERT job_positions OFF;
GO

-- ================================================================
-- 3. INSERT EMPLOYEES (cần có trước khi test)
-- ================================================================
SET IDENTITY_INSERT employees ON;
INSERT INTO employees (emp_id, user_id, full_name, gender, dob, phone, dept_id, job_id, status, join_date) VALUES
(1, NULL, N'Nguyễn Văn A', 'MALE', '1995-05-15', '0901234567', 1, 1, 'OFFICIAL', '2022-01-10'),
(2, NULL, N'Trần Thị B', 'FEMALE', '1996-08-20', '0902345678', 1, 1, 'OFFICIAL', '2022-03-15'),
(3, NULL, N'Lê Văn C', 'MALE', '1994-12-10', '0903456789', 2, 2, 'OFFICIAL', '2021-06-01'),
(10, NULL, N'Manager Nguyễn', 'MALE', '1985-01-01', '0909999999', 1, 1, 'OFFICIAL', '2020-01-01');
SET IDENTITY_INSERT employees OFF;
GO

-- ================================================================
-- 4. INSERT EVAL CYCLES (Chu kỳ đánh giá)
-- ================================================================
SET IDENTITY_INSERT eval_cycles ON;
INSERT INTO eval_cycles (cycle_id, cycle_name, start_date, end_date, is_active) VALUES
(1, N'Q1 2024', '2024-01-01', '2024-03-31', 1),
(2, N'Q2 2024', '2024-04-01', '2024-06-30', 1);
SET IDENTITY_INSERT eval_cycles OFF;
GO

-- ================================================================
-- 5. INSERT KPI TEMPLATES
-- ================================================================
SET IDENTITY_INSERT kpi_templates ON;
INSERT INTO kpi_templates (kpi_id, kpi_name, description, weight, created_by) VALUES
(1, N'Communication Skills', N'Khả năng giao tiếp', 20.00, 10),
(2, N'Technical Skills - Java', N'Kỹ năng lập trình Java', 30.00, 10),
(3, N'Problem Solving', N'Giải quyết vấn đề', 25.00, 10),
(4, N'Teamwork', N'Làm việc nhóm', 15.00, 10),
(5, N'Time Management', N'Quản lý thời gian', 10.00, 10);
SET IDENTITY_INSERT kpi_templates OFF;
GO

-- ================================================================
-- 6. INSERT KPI ASSIGNMENTS (Gán KPI cho nhân viên trong cycle)
-- ================================================================
SET IDENTITY_INSERT kpi_assignments ON;
INSERT INTO kpi_assignments (assignment_id, cycle_id, kpi_id, emp_id, dept_id, target_value, min_threshold, max_threshold, assigned_by) VALUES
-- Employee 1 (Nguyễn Văn A)
(1, 1, 1, 1, NULL, 80.00, 60.00, 100.00, 10),
(2, 1, 2, 1, NULL, 85.00, 70.00, 100.00, 10),
(3, 1, 3, 1, NULL, 75.00, 60.00, 100.00, 10),
(4, 1, 4, 1, NULL, 80.00, 60.00, 100.00, 10),
(5, 1, 5, 1, NULL, 70.00, 50.00, 100.00, 10),

-- Employee 2 (Trần Thị B)
(6, 1, 1, 2, NULL, 80.00, 60.00, 100.00, 10),
(7, 1, 2, 2, NULL, 85.00, 70.00, 100.00, 10),
(8, 1, 3, 2, NULL, 75.00, 60.00, 100.00, 10),
(9, 1, 4, 2, NULL, 80.00, 60.00, 100.00, 10),
(10, 1, 5, 2, NULL, 70.00, 50.00, 100.00, 10);
SET IDENTITY_INSERT kpi_assignments OFF;
GO

-- ================================================================
-- 7. INSERT TRAINING PROGRAMS
-- ================================================================
SET IDENTITY_INSERT training_programs ON;
INSERT INTO training_programs (program_id, program_code, program_name, description, duration_hours, skill_category, level, max_participants, status, created_by) VALUES
(1, 'COMM-101', N'Kỹ năng Giao tiếp Cơ bản', N'Khóa học giao tiếp hiệu quả', 40, N'Communication Skills', 'BEGINNER', 30, 'ACTIVE', 3),
(2, 'COMM-201', N'Kỹ năng Giao tiếp Nâng cao', N'Giao tiếp chuyên nghiệp', 60, N'Communication Skills', 'ADVANCED', 20, 'ACTIVE', 3),
(3, 'TECH-JAVA-101', N'Java Programming Foundation', N'Lập trình Java cơ bản', 80, N'Technical Skills - Java', 'BEGINNER', 25, 'ACTIVE', 3),
(4, 'TECH-JAVA-201', N'Advanced Java & Spring Boot', N'Java nâng cao và Spring Boot', 120, N'Technical Skills - Java', 'ADVANCED', 20, 'ACTIVE', 3),
(5, 'PROB-101', N'Problem Solving Techniques', N'Kỹ thuật giải quyết vấn đề', 40, N'Problem Solving', 'INTERMEDIATE', 30, 'ACTIVE', 3),
(6, 'TEAM-101', N'Teamwork & Collaboration', N'Làm việc nhóm hiệu quả', 30, N'Teamwork', 'BEGINNER', 40, 'ACTIVE', 3),
(7, 'TIME-101', N'Time Management Essentials', N'Quản lý thời gian hiệu quả', 20, N'Time Management', 'BEGINNER', 50, 'ACTIVE', 3);
SET IDENTITY_INSERT training_programs OFF;
GO

-- ================================================================
-- 8. SAMPLE EVALUATION DATA (Optional - để test view history)
-- ================================================================
-- Bạn có thể insert mẫu evaluations đã hoàn thành từ các cycle trước
-- Để test chức năng "xem lịch sử đánh giá"

SET IDENTITY_INSERT employee_evaluations ON;
INSERT INTO employee_evaluations (eval_id, cycle_id, emp_id, manager_id, self_score, manager_score, final_score, classification, manager_comment, status) VALUES
-- Evaluation Q4 2023 (đã hoàn thành) - Employee 1 đạt B
(100, 0, 1, 10, 75.00, 72.00, 73.50, 'B', N'Hoàn thành tốt nhiệm vụ, cần cải thiện kỹ năng giao tiếp', 'COMPLETED'),
-- Evaluation Q3 2023 - Employee 1 đạt C
(101, 0, 1, 10, 65.00, 60.00, 62.50, 'C', N'Cần cố gắng thêm', 'COMPLETED');
SET IDENTITY_INSERT employee_evaluations OFF;
GO

-- ================================================================
-- SUMMARY
-- ================================================================
/*
Data đã insert:
✅ 3 Departments (IT, HR, Sales)
✅ 3 Job Positions
✅ 4 Employees (emp_id: 1, 2, 3, 10)
✅ 2 Eval Cycles (Q1 2024, Q2 2024)
✅ 5 KPI Templates
✅ 10 KPI Assignments (5 cho Employee 1, 5 cho Employee 2)
✅ 7 Training Programs (các skill categories khác nhau)
✅ 2 Sample evaluations (history)

Bây giờ có thể test:
1. Tạo evaluation mới cho Employee 1, Cycle 1
2. Submit self-score
3. Manager approve
4. Auto create recommendations
5. Assign training
6. Complete training & upload certificate
7. Verify certificate
*/

-- ================================================================
-- VERIFICATION QUERIES (Kiểm tra data)
-- ================================================================
SELECT 'Employees' AS TableName, COUNT(*) AS RecordCount FROM employees
UNION ALL
SELECT 'Departments', COUNT(*) FROM departments
UNION ALL
SELECT 'Eval Cycles', COUNT(*) FROM eval_cycles
UNION ALL
SELECT 'KPI Templates', COUNT(*) FROM kpi_templates
UNION ALL
SELECT 'KPI Assignments', COUNT(*) FROM kpi_assignments
UNION ALL
SELECT 'Training Programs', COUNT(*) FROM training_programs;
GO

PRINT '✅ Seed data inserted successfully!';
GO
