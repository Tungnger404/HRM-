package com.example.hrm.controller;

import com.example.hrm.dto.ChangeRequestForm;
import com.example.hrm.entity.Employee;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.EmployeeChangeRequestService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/employee/change-requests")
public class EmployeeChangeRequestController {

    private final EmployeeChangeRequestService changeReqService;
    private final CurrentEmployeeService currentEmployeeService;

    public EmployeeChangeRequestController(EmployeeChangeRequestService changeReqService,
                                           CurrentEmployeeService currentEmployeeService) {
        this.changeReqService = changeReqService;
        this.currentEmployeeService = currentEmployeeService;
    }

    private Map<String, String> fieldOptions() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("phone", "Phone");
        m.put("address", "Address");
        m.put("identityCard", "Identity Card");
        m.put("taxCode", "Tax Code");
        return m;
    }

    // 1) Form submit request
    @GetMapping("/new")
    public String createForm(Model model, Principal principal) {
        Employee emp = currentEmployeeService.requireEmployee(principal);

        ChangeRequestForm form = new ChangeRequestForm();
        form.setEmployeeId(emp.getId());

        model.addAttribute("employee", emp);
        model.addAttribute("form", form);
        model.addAttribute("fieldOptions", fieldOptions());

        return "employee/change-request-create";
    }

    // 2) Submit request
    @PostMapping("/new")
    public String submit(@Valid @ModelAttribute("form") ChangeRequestForm form,
                         BindingResult br,
                         Model model,
                         Principal principal,
                         RedirectAttributes ra) {
        Employee emp = currentEmployeeService.requireEmployee(principal);

        // chống sửa employeeId bằng tay
        form.setEmployeeId(emp.getId());

        if (br.hasErrors()) {
            model.addAttribute("employee", emp);
            model.addAttribute("fieldOptions", fieldOptions());
            return "employee/change-request-create";
        }

        try {
            changeReqService.submit(form);
            ra.addFlashAttribute("msg", "Submitted change request successfully!");
            return "redirect:/employee/change-requests/my";
        } catch (Exception ex) {
            model.addAttribute("employee", emp);
            model.addAttribute("fieldOptions", fieldOptions());
            model.addAttribute("err", ex.getMessage());
            return "employee/change-request-create";
        }
    }

    // 3) My requests
    @GetMapping("/my")
    public String myRequests(Model model, Principal principal) {
        Integer empId = currentEmployeeService.requireCurrentEmpId(principal);

        model.addAttribute("requests", changeReqService.myRequests(empId));
        model.addAttribute("fieldOptions", fieldOptions());

        return "employee/change-request-my";
    }
}
