package com.example.hrm.service;

import com.example.hrm.dto.EmployeeAssignmentUpdateRequest;
import com.example.hrm.entity.Department;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeDepartmentTransferHistory;
import com.example.hrm.entity.EmployeeJobChangeHistory;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeDepartmentTransferHistoryRepository;
import com.example.hrm.repository.EmployeeJobChangeHistoryRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.JobPositionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class HrEmployeeAssignmentService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final EmployeeDepartmentTransferHistoryRepository transferHistoryRepository;
    private final EmployeeJobChangeHistoryRepository jobChangeHistoryRepository;

    public HrEmployeeAssignmentService(EmployeeRepository employeeRepository,
                                       DepartmentRepository departmentRepository,
                                       JobPositionRepository jobPositionRepository,
                                       EmployeeDepartmentTransferHistoryRepository transferHistoryRepository,
                                       EmployeeJobChangeHistoryRepository jobChangeHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.transferHistoryRepository = transferHistoryRepository;
        this.jobChangeHistoryRepository = jobChangeHistoryRepository;
    }

    @Transactional
    public void updateAssignment(Integer empId, EmployeeAssignmentUpdateRequest request) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (request.getDeptId() == null && request.getJobId() == null) {
            throw new RuntimeException("Please select new department or new job");
        }

        Integer oldDeptId = employee.getDeptId();
        Integer oldJobId = employee.getJobId();

        boolean deptChanged = request.getDeptId() != null && !request.getDeptId().equals(oldDeptId);
        boolean jobChanged = request.getJobId() != null && !request.getJobId().equals(oldJobId);

        if (!deptChanged && !jobChanged) {
            throw new RuntimeException("No changes detected");
        }

        String reason = request.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Updated by HR";
        } else {
            reason = reason.trim();
        }

        if (deptChanged) {
            Department newDepartment = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            EmployeeDepartmentTransferHistory transferHistory = EmployeeDepartmentTransferHistory.builder()
                    .empId(employee.getEmpId())
                    .fromDeptId(oldDeptId)
                    .toDeptId(newDepartment.getDeptId())
                    .reason(reason)
                    .transferDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            transferHistoryRepository.save(transferHistory);
            employee.setDeptId(newDepartment.getDeptId());
        }

        if (jobChanged) {
            JobPosition newJob = jobPositionRepository.findById(request.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job position not found"));

            if (newJob.getActive() == null || !newJob.getActive()) {
                throw new RuntimeException("Selected job is not active");
            }

            EmployeeJobChangeHistory jobHistory = EmployeeJobChangeHistory.builder()
                    .empId(employee.getEmpId())
                    .fromJobId(oldJobId)
                    .toJobId(newJob.getJobId())
                    .reason(reason)
                    .changeDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            jobChangeHistoryRepository.save(jobHistory);
            employee.setJobId(newJob.getJobId());
        }

        employeeRepository.save(employee);
    }
}