package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeDocument;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.EmployeeDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/employee/documents")
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;
    private final DocumentStorageService storageService;
    private final CurrentEmployeeService currentEmployeeService;

    public EmployeeDocumentController(EmployeeDocumentService documentService,
                                      DocumentStorageService storageService,
                                      CurrentEmployeeService currentEmployeeService) {
        this.documentService = documentService;
        this.storageService = storageService;
        this.currentEmployeeService = currentEmployeeService;
    }

    @GetMapping
    public String myDocuments(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String docType,
                              @RequestParam(required = false) String status,
                              Principal principal,
                              Model model) {

        Employee emp = currentEmployeeService.requireEmployee(principal);
        Integer empId = emp.getEmpId();

        List<EmployeeDocument> docs = documentService.search(empId, docType, status, q);

        model.addAttribute("docs", docs);
        model.addAttribute("q", q);
        model.addAttribute("docType", docType);
        model.addAttribute("status", status);

        return "employee/my-documents";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam(required = false) String title,
                         @RequestParam(defaultValue = "DOCUMENT") String docType,
                         @RequestParam(defaultValue = "DRAFT") String status,
                         @RequestParam("file") MultipartFile file,
                         Principal principal,
                         RedirectAttributes ra) {

        try {
            Employee emp = currentEmployeeService.requireEmployee(principal);
            Integer empId = emp.getEmpId();
            Integer uploaderUserId = currentEmployeeService.requireUserId(principal);

            documentService.upload(empId, title, docType, status, file, uploaderUserId);

            ra.addFlashAttribute("msg", "Upload thành công.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "Upload thất bại: " + ex.getMessage());
        }
        return "redirect:/employee/documents";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id,
                                             Principal principal) {
        Employee emp = currentEmployeeService.requireEmployee(principal);

        EmployeeDocument doc = documentService.get(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }

        // chỉ cho employee download doc của mình
        if (doc.getEmployeeId() == null || !doc.getEmployeeId().equals(emp.getEmpId())) {
            return ResponseEntity.status(403).build();
        }

        Resource resource = storageService.loadAsResource(doc.getStoredPath());

        String filename = (doc.getFileName() != null && !doc.getFileName().isBlank())
                ? doc.getFileName()
                : ("document-" + id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, doc.getContentType() != null ? doc.getContentType() : "application/octet-stream")
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id,
                         Principal principal,
                         RedirectAttributes ra) {

        try {
            Employee emp = currentEmployeeService.requireEmployee(principal);

            EmployeeDocument doc = documentService.get(id);
            if (doc == null) {
                ra.addFlashAttribute("err", "Không tìm thấy tài liệu.");
                return "redirect:/employee/documents";
            }

            if (doc.getEmployeeId() == null || !doc.getEmployeeId().equals(emp.getEmpId())) {
                ra.addFlashAttribute("err", "Bạn không có quyền xoá tài liệu này.");
                return "redirect:/employee/documents";
            }

            documentService.delete(id);
            ra.addFlashAttribute("msg", "Đã xoá tài liệu.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "Xoá thất bại: " + ex.getMessage());
        }

        return "redirect:/employee/documents";
    }
}