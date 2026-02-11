package com.example.hrm.service.impl;

import com.example.hrm.dto.ChangeRequestForm;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeChangeRequest;
import com.example.hrm.repository.EmployeeChangeRequestRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.EmployeeChangeRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeChangeRequestServiceImpl implements EmployeeChangeRequestService {

    private final EmployeeChangeRequestRepository reqRepo;
    private final EmployeeRepository empRepo;

    public EmployeeChangeRequestServiceImpl(EmployeeChangeRequestRepository reqRepo, EmployeeRepository empRepo) {
        this.reqRepo = reqRepo;
        this.empRepo = empRepo;
    }

    @Override
    @Transactional
    public EmployeeChangeRequest submit(ChangeRequestForm form) {
        Employee e = empRepo.findById(form.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + form.getEmployeeId()));

        String oldVal = getFieldValue(e, form.getFieldKey());

        EmployeeChangeRequest r = new EmployeeChangeRequest();
        r.setEmployeeId(form.getEmployeeId());
        r.setFieldKey(form.getFieldKey());
        r.setOldValue(oldVal);
        r.setNewValue(form.getNewValue());
        r.setReason(form.getReason());
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());

        return reqRepo.save(r);
    }

    @Override
    public List<EmployeeChangeRequest> myRequests(Integer employeeId) {
        return reqRepo.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Override
    public List<EmployeeChangeRequest> pending() {
        return reqRepo.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Override
    @Transactional
    public void approve(Integer requestId, Integer approverUserId, String decisionNote) {
        EmployeeChangeRequest r = reqRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(r.getStatus())) throw new IllegalStateException("Request already decided");

        Employee e = empRepo.findById(r.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + r.getEmployeeId()));

        setFieldValue(e, r.getFieldKey(), r.getNewValue());
        empRepo.save(e);

        r.setStatus("APPROVED");
        r.setApproverUserId(approverUserId);
        r.setDecisionNote(decisionNote);
        r.setDecidedAt(LocalDateTime.now());
        reqRepo.save(r);
    }

    @Override
    @Transactional
    public void reject(Integer requestId, Integer approverUserId, String decisionNote) {
        EmployeeChangeRequest r = reqRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(r.getStatus())) throw new IllegalStateException("Request already decided");

        r.setStatus("REJECTED");
        r.setApproverUserId(approverUserId);
        r.setDecisionNote(decisionNote);
        r.setDecidedAt(LocalDateTime.now());
        reqRepo.save(r);
    }

    private String getFieldValue(Employee e, String fieldKey) {
        return switch (fieldKey) {
            case "phone" -> e.getPhone();
            case "address" -> e.getAddress();
            case "identityCard" -> e.getIdentityCard();
            case "taxCode" -> e.getTaxCode();
            default -> throw new IllegalArgumentException("Unsupported fieldKey: " + fieldKey);
        };
    }

    private void setFieldValue(Employee e, String fieldKey, String newValue) {
        switch (fieldKey) {
            case "phone" -> e.setPhone(newValue);
            case "address" -> e.setAddress(newValue);
            case "identityCard" -> e.setIdentityCard(newValue);
            case "taxCode" -> e.setTaxCode(newValue);
            default -> throw new IllegalArgumentException("Unsupported fieldKey: " + fieldKey);
        }
    }
}
