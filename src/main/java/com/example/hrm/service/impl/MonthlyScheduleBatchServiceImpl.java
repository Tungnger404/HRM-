package com.example.hrm.service.impl;

import com.example.hrm.dto.MonthlyScheduleBatchFormDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.MonthlyScheduleBatch;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.MonthlyScheduleBatchRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.MonthlyScheduleBatchService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyScheduleBatchServiceImpl implements MonthlyScheduleBatchService {

    private final MonthlyScheduleBatchRepository repository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public MonthlyScheduleBatchServiceImpl(MonthlyScheduleBatchRepository repository,
                                           UserRepository userRepository,
                                           EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<MonthlyScheduleBatch> findAll() {
        return repository.findAllByOrderByScheduleMonthDescBatchIdDesc();
    }

    @Override
    public MonthlyScheduleBatch create(MonthlyScheduleBatchFormDTO form) {
        if (form.getScheduleMonth() == null || form.getScheduleMonth().isBlank()) {
            throw new RuntimeException("Schedule month is required.");
        }

        YearMonth ym;
        try {
            ym = YearMonth.parse(form.getScheduleMonth());
        } catch (Exception e) {
            throw new RuntimeException("Invalid month format. Expected yyyy-MM.");
        }

        Integer createdByEmpId = getEmpIdFromSecurity();

        MonthlyScheduleBatch batch = MonthlyScheduleBatch.builder()
                .scheduleMonth(LocalDate.of(ym.getYear(), ym.getMonthValue(), 1))
                .status("DRAFT")
                .createdBy(createdByEmpId)
                .note(form.getNote())
                .build();

        return repository.save(batch);
    }

    private Integer getEmpIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }
}