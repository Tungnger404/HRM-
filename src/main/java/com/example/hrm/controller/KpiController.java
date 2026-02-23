package com.example.hrm.controller;

import com.example.hrm.entity.KpiTemplate;
import com.example.hrm.entity.KpiAssignment;  // ✅ THÊM IMPORT NÀY
import com.example.hrm.service.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    @Autowired
    private KpiService kpiService;

    // === KPI Template ===

    @PostMapping("/templates")
    public ResponseEntity<KpiTemplate> createKpiTemplate(@RequestBody KpiTemplate kpiTemplate) {
        try {
            KpiTemplate created = kpiService.createKpiTemplate(kpiTemplate);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/templates/{kpiId}")
    public ResponseEntity<KpiTemplate> updateKpiTemplate(@PathVariable Integer kpiId,
                                                         @RequestBody KpiTemplate kpiTemplate) {
        try {
            KpiTemplate updated = kpiService.updateKpiTemplate(kpiId, kpiTemplate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<List<KpiTemplate>> getAllTemplates() {
        return ResponseEntity.ok(kpiService.getAllTemplates());
    }

    @GetMapping("/templates/{kpiId}")
    public ResponseEntity<KpiTemplate> getTemplateById(@PathVariable Integer kpiId) {
        Optional<KpiTemplate> template = kpiService.getTemplateById(kpiId);
        return template.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === KPI Assignment ===

    @PostMapping("/assign/employee")
    public ResponseEntity<KpiAssignment> assignKpiToEmployee(@RequestBody KpiAssignment assignment) {
        try {
            KpiAssignment created = kpiService.assignKpiToEmployee(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/assign/department")
    public ResponseEntity<KpiAssignment> assignKpiToDepartment(@RequestBody KpiAssignment assignment) {
        try {
            KpiAssignment created = kpiService.assignKpiToDepartment(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/assignments/employee/{empId}/cycle/{cycleId}")
    public ResponseEntity<List<KpiAssignment>> getAssignmentsByEmployee(@PathVariable Integer empId,
                                                                        @PathVariable Integer cycleId) {
        return ResponseEntity.ok(kpiService.getAssignmentsByEmployee(empId, cycleId));
    }

    @GetMapping("/assignments/department/{deptId}/cycle/{cycleId}")
    public ResponseEntity<List<KpiAssignment>> getAssignmentsByDepartment(@PathVariable Integer deptId,
                                                                          @PathVariable Integer cycleId) {
        return ResponseEntity.ok(kpiService.getAssignmentsByDepartment(deptId, cycleId));
    }

    @GetMapping("/assignments/cycle/{cycleId}")
    public ResponseEntity<List<KpiAssignment>> getAssignmentsByCycle(@PathVariable Integer cycleId) {
        return ResponseEntity.ok(kpiService.getAssignmentsByCycle(cycleId));
    }
}