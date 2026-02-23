package com.example.hrm.controller;

import com.example.hrm.entity.LeaveOrOtRequest;
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
    public String showForm(Model model) {
        model.addAttribute("request", new LeaveOrOtRequest());
        model.addAttribute("sidebar", "sidebar-employee.html");
        return "employee/request-create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute LeaveOrOtRequest request,
                         Model model,
                         HttpSession session) {

        try {
            Integer empId = (Integer) session.getAttribute("empId");

            if (empId == null) {
                return "redirect:/login";
            }

            request.setEmpId(empId);

            service.create(request);

            return "redirect:/employee/requests/create?success";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "employee/request-create";
        }
    }
}