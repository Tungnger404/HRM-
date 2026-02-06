package com.example.hrm.service.impl;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeServiceImpl(EmployeeRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Employee> list(String q) {
        if (q == null || q.trim().isEmpty()) return repo.findAll();
        return repo.findByFullNameContainingIgnoreCase(q.trim());
    }

    @Override
    public Employee getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    @Override
    @Transactional
    public Employee create(EmployeeAdd form) {
        validate(form, false);

        Employee e = new Employee();
        applyForm(e, form);

        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("PROBATION");
        }
        return repo.save(e);
    }

    @Override
    @Transactional
    public Employee update(EmployeeAdd form) {
        validate(form, true);

        Employee e = getById(form.getEmpId());
        applyForm(e, form);

        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("PROBATION");
        }
        return repo.save(e);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }
        // Lưu ý: nếu employee đang được tham chiếu (direct_manager_id, dept.manager_id, ...), DB sẽ chặn delete.
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

    private void applyForm(Employee e, EmployeeAdd f) {
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

    private void validate(EmployeeAdd f, boolean requireId) {
        if (requireId && f.getEmpId() == null) {
            throw new IllegalArgumentException("Missing empId");
        }
        if (f.getFullName() == null || f.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        // chặn tự làm manager của chính mình
        if (f.getEmpId() != null && f.getDirectManagerId() != null && f.getEmpId().equals(f.getDirectManagerId())) {
            throw new IllegalArgumentException("directManagerId cannot be itself");
        }
    }
}
