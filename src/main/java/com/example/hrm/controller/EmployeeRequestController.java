package com.example.hrm.controller;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestType;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.LeaveOrOtRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final LeaveOrOtRequestService service;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping("/create")
    public String showForm(Model model,
                           @RequestParam(value = "success", required = false) String success) {

        model.addAttribute("request", new LeaveOrOtRequest());
        model.addAttribute("types", new RequestType[]{
                RequestType.LEAVE,
                RequestType.OVERTIME,
                RequestType.OTHER
        });
        model.addAttribute("sidebar", "sidebar-employee.html");

        if (success != null) {
            model.addAttribute("message", "Request submitted successfully!");
        }

        return "employee/request-create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("request") LeaveOrOtRequest request,
                         @RequestParam(value = "attachment", required = false) MultipartFile file,
                         Principal principal,
                         Model model) {
        try {
            Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
            request.setEmpId(empId);

            if (request.getTargetWorkDate() == null) {
                throw new RuntimeException("Target work date is required.");
            }
            if (request.getRequestType() == null) {
                throw new RuntimeException("Request type is required.");
            }

            if (file != null && !file.isEmpty() && request.getRequestType() == RequestType.LEAVE) {
                String uploadDir = "uploads/";
                File dir = new File(uploadDir);

                if (!dir.exists() && !dir.mkdirs()) {
                    throw new RuntimeException("Could not create upload directory");
                }

                File destFile = new File(dir.getAbsolutePath(), file.getOriginalFilename());
                file.transferTo(destFile);
                request.setAttachmentPath(uploadDir + file.getOriginalFilename());
            }

            request.setStatus(null); // @PrePersist -> PENDING
            service.create(request);
            return "redirect:/employee/requests/create?success";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("request", request);
            model.addAttribute("types", new RequestType[]{
                    RequestType.LEAVE,
                    RequestType.OVERTIME,
                    RequestType.OTHER
            });
            model.addAttribute("sidebar", "sidebar-employee.html");
            return "employee/request-create";
        }
    }
    @GetMapping("/my")
    public String myRequests(Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);
        model.addAttribute("requests", service.getMyRequests(empId));
        return "employee/request-my";
    }
}
