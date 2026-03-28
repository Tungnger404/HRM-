package com.example.hrm.controller;

import com.example.hrm.entity.EvalCycle;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.EvalCycleRepository;
import com.example.hrm.repository.KpiAssignmentRepository;
import com.example.hrm.repository.KpiEvidenceRepository;
import com.example.hrm.repository.KpiTemplateRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.FileValidationService;
import com.example.hrm.service.KpiEvidenceService;
import com.example.hrm.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.Principal;

@Controller
@RequestMapping("/hr/kpi")
public class HrKpiViewController {

    private static final Logger log = LoggerFactory.getLogger(HrKpiViewController.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final KpiAssignmentRepository kpiAssignmentRepository;
    private final KpiEvidenceRepository kpiEvidenceRepository;
    private final EvalCycleRepository evalCycleRepository;
    private final DocumentStorageService documentStorageService;
    private final FileValidationService fileValidationService;
    private final NotificationService notificationService;
    private final KpiEvidenceService kpiEvidenceService;
    private final CurrentEmployeeService currentEmployeeService;
    private final KpiTemplateRepository kpiTemplateRepository;
    private final JobPositionRepository jobPositionRepository;

    public HrKpiViewController(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               KpiAssignmentRepository kpiAssignmentRepository,
                               KpiEvidenceRepository kpiEvidenceRepository,
                               EvalCycleRepository evalCycleRepository,
                               DocumentStorageService documentStorageService,
                               FileValidationService fileValidationService,
                               NotificationService notificationService,
                               KpiEvidenceService kpiEvidenceService,
                               CurrentEmployeeService currentEmployeeService,
                               KpiTemplateRepository kpiTemplateRepository,
                               JobPositionRepository jobPositionRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.kpiAssignmentRepository = kpiAssignmentRepository;
        this.kpiEvidenceRepository = kpiEvidenceRepository;
        this.evalCycleRepository = evalCycleRepository;
        this.documentStorageService = documentStorageService;
        this.fileValidationService = fileValidationService;
        this.notificationService = notificationService;
        this.kpiEvidenceService = kpiEvidenceService;
        this.currentEmployeeService = currentEmployeeService;
        this.kpiTemplateRepository = kpiTemplateRepository;
        this.jobPositionRepository = jobPositionRepository;
    }

    @GetMapping("/configure")
    public String showConfigurePage(Model model,
                                    @ModelAttribute("msg") String msg,
                                    @ModelAttribute("err") String err) {
        List<EvalCycle> cycles = evalCycleRepository.findAllByOrderByStartDateDesc();
        model.addAttribute("cycles", cycles);
        model.addAttribute("departments", departmentRepository.findAll());

        List<Employee> allEmployees = employeeRepository.findAllEmployeesOnly();
        int allEmployeesCount = allEmployees.size();

        Map<Integer, String> deptNames = departmentRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        d -> d.getDeptId(),
                        d -> d.getDeptName() != null ? d.getDeptName() : "-"
                ));
        Map<Integer, String> positionNames = jobPositionRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getJobId(),
                        p -> p.getTitle() != null ? p.getTitle() : "-"
                ));

        List<Map<String, Object>> employeeOptions = new ArrayList<>();
        for (Employee emp : allEmployees) {
            Map<String, Object> item = new HashMap<>();
            item.put("empId", emp.getEmpId());
            item.put("fullName", emp.getFullName());
            item.put("deptName", emp.getDeptId() != null ? deptNames.getOrDefault(emp.getDeptId(), "-") : "-");
            item.put("positionTitle", emp.getJobId() != null ? positionNames.getOrDefault(emp.getJobId(), "-") : "-");
            employeeOptions.add(item);
        }
        
        log.info("Total EMPLOYEE role users in system: {}", allEmployeesCount);
        model.addAttribute("allEmployeesCount", allEmployeesCount);
        model.addAttribute("employeeOptions", employeeOptions);
        return "hr/kpi_configure";
    }

    @PostMapping("/configure/upload")
    @Transactional
    public String uploadKpiTemplate(@RequestParam Integer cycleId,
                                    @RequestParam String applyScope,
                                    @RequestParam(required = false) Integer empId,
                                    @RequestParam(required = false) Integer deptId,
                                    @RequestParam(required = false) String hrComment,
                                    @RequestParam MultipartFile excelFile,
                                    Principal principal,
                                    RedirectAttributes ra) {

        try {
            if (excelFile == null || excelFile.isEmpty()) {
                ra.addFlashAttribute("err", "Please select an Excel file");
                return "redirect:/hr/kpi/configure";
            }

            if (!fileValidationService.isValidExcelFile(excelFile)) {
                ra.addFlashAttribute("err", "Invalid file. Only .xlsx or .xls accepted");
                return "redirect:/hr/kpi/configure";
            }

            if (!fileValidationService.validateFileSize(excelFile, 10)) {
                ra.addFlashAttribute("err", "File exceeds 10MB");
                return "redirect:/hr/kpi/configure";
            }

            String storedPath = documentStorageService.store(excelFile);
            List<Employee> targetEmployees;
            Integer currentHrUserId = currentEmployeeService.requireUserId(principal);

            switch (applyScope) {
                case "ALL":
                    targetEmployees = employeeRepository.findAllEmployeesOnly();
                    log.info("Sending KPI to ALL EMPLOYEE role users: {} employees", targetEmployees.size());
                    break;

                case "DEPARTMENT":
                    if (deptId == null) {
                        ra.addFlashAttribute("err", "Please select a department");
                        return "redirect:/hr/kpi/configure";
                    }
                    targetEmployees = employeeRepository.findEmployeesOnlyByDeptId(deptId);
                    log.info("Sending KPI to department {}: {} employees", deptId, targetEmployees.size());
                    break;

                case "EMPLOYEE":
                    if (empId == null) {
                        ra.addFlashAttribute("err", "Please select an employee");
                        return "redirect:/hr/kpi/configure";
                    }
                    Employee one = employeeRepository.findEmployeeOnlyByEmpId(empId)
                            .orElseThrow(() -> new RuntimeException("Selected user is not an EMPLOYEE role user"));
                    targetEmployees = List.of(one);
                    break;

                default:
                    ra.addFlashAttribute("err", "Invalid apply scope");
                    return "redirect:/hr/kpi/configure";
            }

            if (targetEmployees.isEmpty()) {
                ra.addFlashAttribute("err", "No employees found for selected scope");
                return "redirect:/hr/kpi/configure";
            }

            Integer defaultKpiId;
            try {
                defaultKpiId = resolveDefaultKpiId();
            } catch (RuntimeException ex) {
                ra.addFlashAttribute("toastError", "Cannot send KPI because no KPI template exists. Please create at least one KPI template first.");
                return "redirect:/hr/kpi/configure";
            }

            int successCount = 0;

            for (Employee emp : targetEmployees) {
                try {
                    KpiAssignment existingAssignment = kpiAssignmentRepository
                            .findByEmpIdAndCycleId(emp.getEmpId(), cycleId)
                            .stream()
                            .findFirst()
                            .orElse(null);

                    KpiAssignment assignment;
                    if (existingAssignment != null) {
                        log.info("Updating existing assignment for emp_id: {}", emp.getEmpId());
                        assignment = existingAssignment;
                        assignment.setHrExcelTemplatePath(storedPath);
                        assignment.setHrComment(hrComment);
                        assignment.setStatus(KpiAssignment.AssignmentStatus.ASSIGNED);
                        assignment.setAssignedAt(LocalDateTime.now());
                        assignment.setAssignedBy(currentHrUserId);
                        if ("EMPLOYEE".equals(applyScope)) {
                            assignment.setDeptId(null);
                        }
                    } else {
                        log.info("Creating new assignment for emp_id: {}", emp.getEmpId());
                        assignment = new KpiAssignment();
                        assignment.setCycleId(cycleId);
                        assignment.setKpiId(defaultKpiId);
                        assignment.setEmpId(emp.getEmpId());
                        assignment.setDeptId("EMPLOYEE".equals(applyScope) ? null : emp.getDeptId());
                        assignment.setHrExcelTemplatePath(storedPath);
                        assignment.setHrComment(hrComment);
                        assignment.setStatus(KpiAssignment.AssignmentStatus.ASSIGNED);
                        assignment.setAssignedAt(LocalDateTime.now());
                        assignment.setAssignedBy(currentHrUserId);
                    }

                    KpiAssignment saved = kpiAssignmentRepository.save(assignment);
                    log.info("Saved assignment ID: {} for emp_id: {}", saved.getAssignmentId(), emp.getEmpId());

                    notificationService.createKpiAssignmentNotification(
                            emp.getEmpId(),
                            saved.getAssignmentId(),
                            hrComment != null ? hrComment : ""
                    );

                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to assign KPI to emp_id: {}, error: {}", emp.getEmpId(), e.getMessage());
                }
            }

            if (successCount > 0) {
                int failedCount = targetEmployees.size() - successCount;
                if (failedCount > 0) {
                    ra.addFlashAttribute("toastSuccess", "KPI template sent to " + successCount + " employee(s). "
                            + failedCount + " failed.");
                } else {
                    ra.addFlashAttribute("toastSuccess", "KPI template sent successfully to " + successCount + " employee(s).");
                }
            } else {
                ra.addFlashAttribute("toastError", "Failed to send KPI template to all selected employees.");
            }

            return "redirect:/hr/kpi/pending-verification";

        } catch (Exception e) {
            log.error("Upload KPI template failed", e);
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/configure";
        }
    }

    private Integer resolveDefaultKpiId() {
        return kpiTemplateRepository.findAllByOrderByKpiNameAsc().stream()
                .findFirst()
                .map(template -> template.getKpiId())
                .orElseThrow(() -> new RuntimeException("No KPI template found"));
    }

    @GetMapping("/pending-verification")
    public String showPendingVerification(Model model,
                                          @ModelAttribute("msg") String msg,
                                          @ModelAttribute("err") String err) {
        List<KpiAssignment> pending = kpiAssignmentRepository.findByStatusIn(List.of(
                KpiAssignment.AssignmentStatus.EMPLOYEE_SUBMITTED,
                KpiAssignment.AssignmentStatus.EMPLOYEE_RESUBMITTED
        ));

        model.addAttribute("assignments", pending);
        return "hr/kpi_pending_verification";
    }

    @GetMapping("/verify/{assignmentId}")
    public String showVerifyPage(@PathVariable Integer assignmentId,
                                 Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err) {
        KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("KPI assignment not found"));

        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignmentId);

        model.addAttribute("assignment", assignment);
        model.addAttribute("evidences", evidences);
        return "hr/kpi_verify";
    }

    @PostMapping("/verify/{assignmentId}/approve")
    public String approveKpi(@PathVariable Integer assignmentId,
                             @RequestParam(required = false) List<String> verificationChecks,
                             @RequestParam(required = false) String hrNote,
                             Principal principal,
                             RedirectAttributes ra) {
        try {
            if (verificationChecks == null || verificationChecks.isEmpty()) {
                ra.addFlashAttribute("err", "Please confirm at least one verification checklist item before forwarding.");
                return "redirect:/hr/kpi/verify/" + assignmentId;
            }

            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));

            Employee employee = employeeRepository.findById(assignment.getEmpId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            Integer managerEmpId = employee.getDirectManagerId();
            if (managerEmpId == null) {
                throw new RuntimeException("Direct manager not found for employee ID: " + employee.getEmpId());
            }

            assignment.setStatus(KpiAssignment.AssignmentStatus.HR_VERIFIED);
            assignment.setHrVerifiedAt(LocalDateTime.now());
            assignment.setHrVerifiedBy(currentEmployeeService.requireUserId(principal));
            assignment.setHrVerificationNote(hrNote);
            kpiAssignmentRepository.save(assignment);

            notificationService.createEvaluationPendingNotification(
                    managerEmpId,
                    assignment.getAssignmentId(),
                    employee.getFullName()
            );

            ra.addFlashAttribute("msg", "Verified and forwarded to Manager");
            return "redirect:/hr/kpi/pending-verification";

        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/verify/" + assignmentId;
        }
    }

    @PostMapping("/verify/{assignmentId}/reject")
    public String rejectKpi(@PathVariable Integer assignmentId,
                            @RequestParam String rejectReason,
                            RedirectAttributes ra) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("KPI assignment not found"));

            assignment.setStatus(KpiAssignment.AssignmentStatus.HR_REJECTED);
            assignment.setHrVerificationNote(rejectReason);
            kpiAssignmentRepository.save(assignment);

            notificationService.createKpiRejectedNotification(
                    assignment.getEmpId(),
                    assignment.getAssignmentId(),
                    rejectReason
            );

            ra.addFlashAttribute("msg", "Returned to Employee for revision");
            return "redirect:/hr/kpi/pending-verification";

        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/kpi/verify/" + assignmentId;
        }
    }

    @GetMapping("/download-template/{assignmentId}")
    public ResponseEntity<Resource> downloadHrTemplate(@PathVariable Integer assignmentId) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId).orElse(null);
            if (assignment == null || assignment.getHrExcelTemplatePath() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = documentStorageService.loadAsResource(assignment.getHrExcelTemplatePath());
            String filename = "KPI_Template_" + assignmentId + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download-employee/{assignmentId}")
    public ResponseEntity<Resource> downloadEmployeeSubmission(@PathVariable Integer assignmentId) {
        try {
            KpiAssignment assignment = kpiAssignmentRepository.findById(assignmentId).orElse(null);
            if (assignment == null || assignment.getEmployeeExcelPath() == null
                    || assignment.getEmployeeExcelPath().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            String path = assignment.getEmployeeExcelPath().trim();
            Resource resource = documentStorageService.loadAsResource(path);
            String filename = "KPI_Employee_" + assignment.getEmpId() + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);

        } catch (Exception e) {
            log.warn("Download employee Excel failed for assignment {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download-evidence/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable Integer evidenceId) {
        try {
            KpiEvidence evidence = kpiEvidenceRepository.findById(evidenceId).orElse(null);
            if (evidence == null || evidence.getStoredPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = documentStorageService.loadAsResource(evidence.getStoredPath());
            String filename = evidence.getFileName() != null ? evidence.getFileName() : "evidence_" + evidenceId;
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

