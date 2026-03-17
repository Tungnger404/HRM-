package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeDocument;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.EmployeeDocumentService;
import com.example.hrm.service.ManagerDepartmentAccessService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/manager/org/documents")
public class ManagerDocumentController {

    private final EmployeeDocumentService docService;
    private final DocumentStorageService storage;
    private final CurrentEmployeeService currentEmployeeService;
    private final ManagerDepartmentAccessService accessService;

    public ManagerDocumentController(EmployeeDocumentService docService,
                                     DocumentStorageService storage,
                                     CurrentEmployeeService currentEmployeeService,
                                     ManagerDepartmentAccessService accessService) {
        this.docService = docService;
        this.storage = storage;
        this.currentEmployeeService = currentEmployeeService;
        this.accessService = accessService;
    }

    @GetMapping
    public String employees(@RequestParam(value = "q", required = false) String q,
                            @RequestParam(value = "status", required = false) String status,
                            Principal principal,
                            Model model) {

        Integer managerDeptId = accessService.currentManagerDeptId(principal);

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("managedDepartments", accessService.getManagedDepartments(principal));
        model.addAttribute("employees",
                docService.findEmployeesForManagerDepartment(managerDeptId, q, status));

        return "manager/org-documents-employees";
    }

    @GetMapping("/{empId}")
    public String detail(@PathVariable Integer empId,
                         @RequestParam(value = "docType", required = false) String docType,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "q", required = false) String q,
                         Principal principal,
                         Model model) {

        Employee employee = accessService.requireManagedEmployeeEntity(empId, principal);
        List<EmployeeDocument> docs = docService.search(empId, docType, status, q);

        model.addAttribute("employee", employee);
        model.addAttribute("docs", docs);
        model.addAttribute("empId", empId);
        model.addAttribute("docType", docType);
        model.addAttribute("status", status);
        model.addAttribute("q", q);

        return "manager/org-documents-list";
    }

    @PostMapping("/{empId}/upload")
    public String upload(@PathVariable Integer empId,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "docType", required = false) String docType,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam("file") MultipartFile file,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            accessService.requireManagedEmployee(empId, principal);

            Integer uploaderUserId = currentEmployeeService.requireUserId(principal);
            docService.upload(empId, title, docType, status, file, uploaderUserId);

            ra.addFlashAttribute("msg", "Uploaded document successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/manager/org/documents/" + empId;
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "docType", required = false) String docType,
                         @RequestParam(value = "status", required = false) String status,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            EmployeeDocument d = docService.get(id);
            accessService.requireManagedEmployee(d.getEmployeeId(), principal);

            EmployeeDocument updated = docService.updateMeta(id, title, docType, status);

            ra.addFlashAttribute("msg", "Updated document successfully.");
            return "redirect:/manager/org/documents/" + updated.getEmployeeId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/org/documents";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            EmployeeDocument d = docService.get(id);
            accessService.requireManagedEmployee(d.getEmployeeId(), principal);

            Integer empId = d.getEmployeeId();
            docService.delete(id);

            ra.addFlashAttribute("msg", "Deleted document successfully.");
            return "redirect:/manager/org/documents/" + empId;
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/org/documents";
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id,
                                             Principal principal,
                                             HttpServletRequest request) {
        EmployeeDocument d = docService.get(id);
        accessService.requireManagedEmployee(d.getEmployeeId(), principal);

        Resource resource = storage.loadAsResource(d.getStoredPath());

        String contentType = d.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        String filename = d.getFileName() == null ? "file" : d.getFileName();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }
}