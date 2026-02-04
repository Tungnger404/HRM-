package com.example.hrm.evaluation.service;

import com.example.hrm.evaluation.model.KpiTemplate;
import com.example.hrm.evaluation.model.KpiAssignment;

import java.util.List;
import java.util.Optional;

public interface KpiService {

    // === KPI Template (managed by HR) ===
    // Create a new KPI template
    KpiTemplate createKpiTemplate(KpiTemplate kpiTemplate);

    // Update an existing KPI template
    KpiTemplate updateKpiTemplate(Integer kpiId, KpiTemplate kpiTemplate);

    // Get all KPI templates
    List<KpiTemplate> getAllTemplates();

    // Get a specific KPI template by ID
    Optional<KpiTemplate> getTemplateById(Integer kpiId);

    // === KPI Assignment (assign KPI to employee or department) ===
    // Assign a KPI to a specific employee for a cycle
    KpiAssignment assignKpiToEmployee(KpiAssignment assignment);

    // Assign a KPI to a department for a cycle
    KpiAssignment assignKpiToDepartment(KpiAssignment assignment);

    // Get all KPI assignments for an employee in a cycle
    List<KpiAssignment> getAssignmentsByEmployee(Integer empId, Integer cycleId);

    // Get all KPI assignments for a department in a cycle
    List<KpiAssignment> getAssignmentsByDepartment(Integer deptId, Integer cycleId);

    // Get all assignments in a cycle
    List<KpiAssignment> getAssignmentsByCycle(Integer cycleId);
}