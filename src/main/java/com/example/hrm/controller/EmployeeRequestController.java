package com.example.hrm.controller;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestType;
import com.example.hrm.service.LeaveOrOtRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final LeaveOrOtRequestService service;

    public EmployeeRequestController(LeaveOrOtRequestService service) {
        this.service = service;
    }

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
    public String create(@ModelAttribute LeaveOrOtRequest request,
                         @RequestParam(value="attachment",required =false) MultipartFile file,
                         Model model,
                         HttpSession session) {

        try {
            Integer empId = (Integer) session.getAttribute("EMP_ID");

            if (empId == null) {
                return "redirect:/login";
            }

            request.setEmpId(empId);

            if (file != null &&!file.isEmpty() && request.getRequestType() == RequestType.LEAVE) {

                String uploadDir = "uploads/";
                File dir = new File(uploadDir);

                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        throw new RuntimeException("Could not create upload directory");
                    }
                }

                String filePath = uploadDir + file.getOriginalFilename();
                file.transferTo(new File(filePath));

                // nếu bạn muốn lưu đường dẫn vào DB
                request.setAttachmentPath(filePath);
            }
            service.create(request);

            return "redirect:/employee/requests/create?success";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "employee/request-create";
        }
    }
}
