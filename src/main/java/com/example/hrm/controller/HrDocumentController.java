package com.example.hrm.controller;

import com.example.hrm.entity.EmployeeDocument;
import com.example.hrm.service.DocumentStorageService;
import com.example.hrm.service.EmployeeDocumentService;
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
import java.util.List;

@Controller
@RequestMapping("/hr/documents")
public class HrDocumentController {

    private final EmployeeDocumentService docService;
    private final DocumentStorageService storage;

    public HrDocumentController(EmployeeDocumentService docService, DocumentStorageService storage) {
        this.docService = docService;
        this.storage = storage;
    }

    @GetMapping
    public String list(@RequestParam(value = "empId", required = false) Integer empId,
                       @RequestParam(value = "docType", required = false) String docType,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "q", required = false) String q,
                       Model model,
                       @ModelAttribute("msg") String msg,
                       @ModelAttribute("err") String err) {

        List<EmployeeDocument> docs = docService.search(empId, docType, status, q);

        model.addAttribute("docs", docs);
        model.addAttribute("empId", empId);
        model.addAttribute("docType", docType);
        model.addAttribute("status", status);
        model.addAttribute("q", q);

        return "hr/documents_list";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("empId") Integer empId,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "docType", required = false) String docType,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam("file") MultipartFile file,
                         RedirectAttributes ra) {
        try {
            docService.upload(empId, title, docType, status, file);
            ra.addFlashAttribute("msg", "Uploaded document!");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/documents?empId=" + empId;
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "docType", required = false) String docType,
                         @RequestParam(value = "status", required = false) String status,
                         RedirectAttributes ra) {
        try {
            EmployeeDocument d = docService.updateMeta(id, title, docType, status);
            ra.addFlashAttribute("msg", "Updated document!");
            return "redirect:/hr/documents?empId=" + d.getEmployeeId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/documents";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            EmployeeDocument d = docService.get(id);
            Integer empId = d.getEmployeeId();
            docService.delete(id);
            ra.addFlashAttribute("msg", "Deleted document!");
            return "redirect:/hr/documents?empId=" + empId;
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/documents";
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id, HttpServletRequest request) {
        EmployeeDocument d = docService.get(id);
        Resource resource = storage.loadAsResource(d.getStoredPath());

        String contentType = d.getContentType();
        if (contentType == null || contentType.isBlank()) contentType = "application/octet-stream";

        String filename = d.getFileName() == null ? "file" : d.getFileName();
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }
}
