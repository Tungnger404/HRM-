package com.example.hrm.service;

import com.example.hrm.dto.ShiftAssignmentFormDTO;
import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final MonthlyScheduleBatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final UserRepository userRepository;

    public ShiftAssignmentService(ShiftAssignmentRepository assignmentRepository,
                                  MonthlyScheduleBatchRepository batchRepository,
                                  EmployeeRepository employeeRepository,
                                  ShiftTemplateRepository shiftTemplateRepository,
                                  UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.batchRepository = batchRepository;
        this.employeeRepository = employeeRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.userRepository = userRepository;
    }

    public List<ShiftAssignment> listByBatch(Long batchId) {
        return assignmentRepository.findByBatch_BatchIdOrderByWorkDateAscEmployee_EmpIdAsc(batchId);
    }

    public void createOrUpdate(ShiftAssignmentFormDTO form) {
        if (form.getBatchId() == null) throw new RuntimeException("Batch is required.");
        if (form.getEmpId() == null) throw new RuntimeException("Employee is required.");
        if (form.getWorkDate() == null || form.getWorkDate().isBlank()) throw new RuntimeException("Work date is required.");
        if (form.getAssignmentType() == null || form.getAssignmentType().isBlank()) throw new RuntimeException("Assignment type is required.");

        MonthlyScheduleBatch batch = batchRepository.findById(form.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Employee employee = employeeRepository.findById(form.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate workDate = LocalDate.parse(form.getWorkDate());

        // ensure selected date belongs to selected month
        if (!YearMonth.from(workDate).equals(YearMonth.from(batch.getScheduleMonth()))) {
            throw new RuntimeException("Work date must be in selected batch month.");
        }

        String type = form.getAssignmentType().toUpperCase();
        ShiftTemplate shift = null;

        if ("WORK".equals(type)) {
            if (form.getShiftId() == null) throw new RuntimeException("Shift is required when assignment type is WORK.");
            shift = shiftTemplateRepository.findById(form.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Shift not found"));
        }

        Integer assignedBy = getEmpIdFromSecurity();

        // upsert by (emp_id, work_date)
        ShiftAssignment assignment = assignmentRepository
                .findByEmployee_EmpIdAndWorkDate(employee.getEmpId(), workDate)
                .orElse(ShiftAssignment.builder()
                        .employee(employee)
                        .workDate(workDate)
                        .build());

        assignment.setBatch(batch);
        assignment.setAssignmentType(type);
        assignment.setShiftTemplate(shift);
        assignment.setAssignedBy(assignedBy);
        assignment.setNote(form.getNote());

        assignmentRepository.save(assignment);
    }

    private Integer getEmpIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }
}