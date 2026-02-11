package com.example.hrm.service.impl;

import com.example.hrm.entity.KpiTemplate;
import com.example.hrm.entity.KpiAssignment;  // ✅ THÊM IMPORT NÀY
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.KpiTemplateRepository;
import com.example.hrm.service.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KpiServiceImpl implements KpiService {

    @Autowired
    private KpiTemplateRepository kpiTemplateRepository;

    @Autowired
    private KpiAssignmentRepository kpiAssignmentRepository;

    // === KPI Template ===

    @Override
    @Transactional
    public KpiTemplate createKpiTemplate(KpiTemplate kpiTemplate) {
        kpiTemplate.setCreatedAt(LocalDateTime.now());
        return kpiTemplateRepository.save(kpiTemplate);
    }

    @Override
    @Transactional
    public KpiTemplate updateKpiTemplate(Integer kpiId, KpiTemplate kpiTemplate) {
        KpiTemplate existing = kpiTemplateRepository.findById(kpiId)
                .orElseThrow(() -> new RuntimeException("KPI Template not found"));

        existing.setKpiName(kpiTemplate.getKpiName());
        existing.setDescription(kpiTemplate.getDescription());
        existing.setWeight(kpiTemplate.getWeight());

        return kpiTemplateRepository.save(existing);
    }

    @Override
    public List<KpiTemplate> getAllTemplates() {
        return kpiTemplateRepository.findAll();
    }

    @Override
    public Optional<KpiTemplate> getTemplateById(Integer kpiId) {
        return kpiTemplateRepository.findById(kpiId);
    }

    // === KPI Assignment ===

    @Override
    @Transactional
    public KpiAssignment assignKpiToEmployee(KpiAssignment assignment) {
        // Validate: empId must be set, deptId must be null
        if (assignment.getEmpId() == null || assignment.getDeptId() != null) {
            throw new RuntimeException("assignKpiToEmployee: empId required, deptId must be null");
        }
        assignment.setAssignedAt(LocalDateTime.now());
        return kpiAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public KpiAssignment assignKpiToDepartment(KpiAssignment assignment) {
        // Validate: deptId must be set, empId must be null
        if (assignment.getDeptId() == null || assignment.getEmpId() != null) {
            throw new RuntimeException("assignKpiToDepartment: deptId required, empId must be null");
        }
        assignment.setAssignedAt(LocalDateTime.now());
        return kpiAssignmentRepository.save(assignment);
    }

    @Override
    public List<KpiAssignment> getAssignmentsByEmployee(Integer empId, Integer cycleId) {
        return kpiAssignmentRepository.findByEmpIdAndCycleId(empId, cycleId);
    }

    @Override
    public List<KpiAssignment> getAssignmentsByDepartment(Integer deptId, Integer cycleId) {
        return kpiAssignmentRepository.findByDeptIdAndCycleId(deptId, cycleId);
    }

    @Override
    public List<KpiAssignment> getAssignmentsByCycle(Integer cycleId) {
        return kpiAssignmentRepository.findByCycleId(cycleId);
    }
}