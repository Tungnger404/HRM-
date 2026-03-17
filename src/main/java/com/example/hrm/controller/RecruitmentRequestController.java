package com.example.hrm.controller;

import com.example.hrm.dto.RecruitmentRequestCreateDTO;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.RecruitmentRequest;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.RecruitmentRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/recruitment-request")
@RequiredArgsConstructor
public class RecruitmentRequestController {

    private final RecruitmentRequestService recruitmentRequestService;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;


    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        RecruitmentRequestCreateDTO dto = new RecruitmentRequestCreateDTO();
        dto.setDeadline(java.time.LocalDate.now().plusDays(7));
        model.addAttribute("dto", dto);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobPositionRepository.findAll());
        return "recruitment-request/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("dto") RecruitmentRequestCreateDTO dto,
                         Principal principal,
                         HttpSession session,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes ra) { // Thêm RedirectAttributes
        if (dto.getDeadline() != null && dto.getDeadline().isBefore(java.time.LocalDate.now())) {
            ra.addFlashAttribute("err", "Hạn chót không được là ngày trong quá khứ!");
            return "redirect:/recruitment-request/create";
        }
        if (dto.getCreatorId() == null && principal != null) {
            Employee emp = currentEmployeeService.requireEmployee(principal);
            dto.setCreatorId(emp.getEmpId());
            session.setAttribute("LOGIN_EMPLOYEE", emp);
        }

        try {
            recruitmentRequestService.createRecruitmentRequest(dto);
            return "redirect:/recruitment-request/create?success=true";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/recruitment-request/create";
        }
    }

    @GetMapping("/my-requests")
    public String myRequests(HttpSession session, Model model, Principal principal) {

        Employee employee = (Employee) session.getAttribute("LOGIN_EMPLOYEE");
        if (employee == null && principal != null) {
            employee = currentEmployeeService.requireEmployee(principal);
            session.setAttribute("LOGIN_EMPLOYEE", employee);
        }
        if (employee == null)
            return "redirect:/login";

        List<RecruitmentRequest> requests =
                recruitmentRequestService.getRequestsByEmployee(employee.getEmpId());

        model.addAttribute("requests", requests);
        return "recruitment-request/my-request";

    }


    @GetMapping("/detail/{id}")
    public String viewManagerDetail(@PathVariable Integer id, Model model) {
        RecruitmentRequest request = recruitmentRequestService.getById(id);
        model.addAttribute("request", request);
        return "recruitment-request/manager-detail-request";
    }
}
