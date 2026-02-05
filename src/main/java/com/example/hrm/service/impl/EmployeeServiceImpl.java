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

        if (e.getEmploymentStatus() == null || e.getEmploymentStatus().isBlank()) {
            e.setEmploymentStatus("Active");
        }

        return repo.save(e);
    }

    @Override
    @Transactional
    public Employee update(EmployeeAdd form) {
        validate(form, true);

        Employee e = getById(form.getEmpId());
        applyForm(e, form);

        if (e.getEmploymentStatus() == null || e.getEmploymentStatus().isBlank()) {
            e.setEmploymentStatus("Active");
        }

        return repo.save(e);
    }

    @Override
    @Transactional
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
        f.setFullName(e.getFullName());
        f.setEmail(e.getEmail());
        f.setPhone(e.getPhone());
        f.setDateOfBirth(e.getDateOfBirth());
        f.setHireDate(e.getHireDate());
        f.setEmploymentStatus(e.getEmploymentStatus());
        return f;
    }

    private void applyForm(Employee e, EmployeeAdd f) {
        e.setFullName(f.getFullName());
        e.setEmail(f.getEmail());
        e.setPhone(f.getPhone());
        e.setDateOfBirth(f.getDateOfBirth());
        e.setHireDate(f.getHireDate());
        e.setEmploymentStatus(f.getEmploymentStatus());
    }

    private void validate(EmployeeAdd f, boolean requireId) {
        if (requireId && f.getEmpId() == null) {
            throw new IllegalArgumentException("Missing empId");
        }
        if (f.getFullName() == null || f.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }
}
