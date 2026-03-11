package com.example.hrm.controller;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestType;
import com.example.hrm.service.AttendanceService;
import com.example.hrm.service.LeaveOrOtRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final LeaveOrOtRequestService service;
    private final AttendanceService attendanceService;

    @GetMapping("/create")
    public String showForm(Model model,
                           @RequestParam(value = "success", required = false) String success) {

        model.addAttribute("request", new LeaveOrOtRequest());
        model.addAttribute("types", RequestType.values());
        model.addAttribute("sidebar", "sidebar-employee.html");

        if (success != null) {
            model.addAttribute("message", "Request submitted successfully!");
        }

        return "employee/request-create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("request") LeaveOrOtRequest request,
                         @RequestParam(value = "attachment", required = false) MultipartFile file,
                         Model model) {
        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            request.setEmpId(empId);

            if (file != null && !file.isEmpty() && request.getRequestType() == RequestType.LEAVE) {
                String uploadDir = "uploads/";
                File dir = new File(uploadDir);

                if (!dir.exists() && !dir.mkdirs()) {
                    throw new RuntimeException("Could not create upload directory");
                }

                String filePath = uploadDir + file.getOriginalFilename();
                file.transferTo(new File(filePath));
                request.setAttachmentPath(filePath);
            }

            service.create(request);
            return "redirect:/employee/requests/create?success";

        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("error", e.getMessage());
            model.addAttribute("request", request);
            model.addAttribute("types", RequestType.values());
            model.addAttribute("sidebar", "sidebar-employee.html");
            return "employee/request-create";
        }
    }
}