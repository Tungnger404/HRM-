package com.example.hrm.service.impl;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.User;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(EmployeeRepository repo,
                               CandidateRepository candidateRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               DepartmentRepository departmentRepository) { // Thêm vào đây
        this.repo = repo;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository; // Và gán ở đây
    }

    @Override
    public List<Employee> list(String q, String status) {
        String qq = (q == null) ? "" : q.trim();
        String st = (status == null) ? "" : status.trim();
        return repo.search(qq, st);
    }

    @Override
    public Employee getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    @Override
    public Employee create(EmployeeAdd form) {
        validate(form, false);

        Employee e = new Employee();
        applyCreateForm(e, form);

        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("PROBATION");
        }

        if (e.getJoinDate() == null) {
            e.setJoinDate(LocalDate.now());
        }

        if (e.getIncludeInPayroll() == null) {
            e.setIncludeInPayroll(false);
        }

        return repo.save(e);
    }

    @Override
    public Employee update(EmployeeAdd form) {
        validate(form, true);

        Employee e = getById(form.getEmpId());

        // Chỉ update general information
        // KHÔNG update deptId / jobId ở màn hình này
        applyGeneralInfoForUpdate(e, form);

        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("PROBATION");
        }

        return repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public EmployeeAdd toForm(Employee e) {
        EmployeeAdd f = new EmployeeAdd();
        f.setEmpId(e.getEmpId());
        f.setUserId(e.getUserId());
        f.setFullName(e.getFullName());
        f.setGender(e.getGender());
        f.setDob(e.getDob());
        f.setPhone(e.getPhone());
        f.setAddress(e.getAddress());
        f.setIdentityCard(e.getIdentityCard());
        f.setTaxCode(e.getTaxCode());
        f.setDeptId(e.getDeptId());
        f.setJobId(e.getJobId());
        f.setDirectManagerId(e.getDirectManagerId());
        f.setStatus(e.getStatus());
        f.setJoinDate(e.getJoinDate());
        return f;
    }

    /**
     * Dùng cho create employee mới
     */
    private void applyCreateForm(Employee e, EmployeeAdd f) {
        e.setUserId(f.getUserId());
        e.setFullName(f.getFullName());
        e.setGender(f.getGender());
        e.setDob(f.getDob());
        e.setPhone(f.getPhone());
        e.setAddress(f.getAddress());
        e.setIdentityCard(f.getIdentityCard());
        e.setTaxCode(f.getTaxCode());
        e.setDeptId(f.getDeptId());
        e.setJobId(f.getJobId());
        e.setDirectManagerId(f.getDirectManagerId());
        e.setStatus(f.getStatus());
        e.setJoinDate(f.getJoinDate());
    }

    /**
     * Dùng cho màn hình HR employee detail
     * Chỉ cập nhật thông tin chung, không đụng department/job
     */
    private void applyGeneralInfoForUpdate(Employee e, EmployeeAdd f) {
        e.setFullName(f.getFullName());
        e.setGender(f.getGender());
        e.setDob(f.getDob());
        e.setPhone(f.getPhone());
        e.setAddress(f.getAddress());
        e.setIdentityCard(f.getIdentityCard());
        e.setTaxCode(f.getTaxCode());
        e.setStatus(f.getStatus());
        e.setJoinDate(f.getJoinDate());

        // KHÔNG set:
        // e.setDeptId(...)
        // e.setJobId(...)
        // e.setUserId(...)
        // e.setDirectManagerId(...)
    }

    private void validate(EmployeeAdd f, boolean requireId) {

        if (requireId && f.getEmpId() == null) {
            throw new IllegalArgumentException("Missing empId");
        }

        if (f.getFullName() == null || f.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (f.getEmpId() != null
                && f.getDirectManagerId() != null
                && f.getEmpId().equals(f.getDirectManagerId())) {
            throw new IllegalArgumentException("directManagerId cannot be itself");
        }
    }

    @Override
    @Transactional
    public void createEmployeeFromCandidate(Integer candidateId, LocalDate joinDate,
                                            Integer jobId, Integer deptId,
                                            String identityCard, String taxCode) {

        Integer managerId = departmentRepository.findManagerIdByDeptId(deptId);

        if (managerId == null) {
            managerId = 3;
        }
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng viên"));


        User user = User.builder()
                .username(candidate.getEmail())
                .passwordHash(passwordEncoder.encode("123456"))
                .email(candidate.getEmail())
                .roleId(3)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);


        Employee employee = Employee.builder()
                .userId(user.getUserId())
                .fullName(candidate.getFullName())
                .phone(candidate.getPhone())
                .gender(candidate.getGender())
                .dob(candidate.getDob())
                .address(candidate.getAddress())
                .identityCard(identityCard)
                .taxCode(taxCode)
                .status("PROBATION")
                .joinDate(joinDate != null ? joinDate : LocalDate.now())
                .includeInPayroll(false)
                .jobId(jobId)
                .deptId(deptId)
                .directManagerId(managerId)
                .build();
        repo.save(employee);

        candidate.setStatus(CandidateStatus.HIRED);
        candidateRepository.save(candidate);
    }

    @Override
    public List<Employee> listManagedByDepartment(Integer managerDeptId, String q, String status) {
        List<Employee> employees = repo.findByDeptIdOrderByFullNameAsc(managerDeptId);

        String keyword = q == null ? "" : q.trim().toLowerCase();
        String st = status == null ? "" : status.trim().toUpperCase();

        return employees.stream()
                .filter(e -> keyword.isBlank()
                        || (e.getFullName() != null && e.getFullName().toLowerCase().contains(keyword)))
                .filter(e -> st.isBlank()
                        || (e.getStatus() != null && e.getStatus().equalsIgnoreCase(st)))
                .toList();
    }
}