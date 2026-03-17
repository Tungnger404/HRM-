package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Employee;
import com.example.hrm.service.EmployeeService;
import com.example.hrm.service.ManagerDepartmentAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/manager/org/employees")
public class ManagerOrganizationController {

    private final EmployeeService employeeService;
    private final ManagerDepartmentAccessService accessService;

    public ManagerOrganizationController(EmployeeService employeeService,
                                         ManagerDepartmentAccessService accessService) {
        this.employeeService = employeeService;
        this.accessService = accessService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       Principal principal,
                       Model model) {

        Integer managerDeptId = accessService.currentManagerDeptId(principal);

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("managedDepartments", accessService.getManagedDepartments(principal));
        model.addAttribute("employees",
                employeeService.listManagedByDepartment(managerDeptId, q, status));

        return "manager/org-employee-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id,
                         Principal principal,
                         Model model) {
        Employee e = accessService.requireManagedEmployeeEntity(id, principal);
        model.addAttribute("e", e);
        return "manager/org-employee-detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute EmployeeAdd form,
                       Principal principal,
                       RedirectAttributes ra) {
        try {
            accessService.requireManagedEmployee(form.getEmpId(), principal);

            Employee current = employeeService.getById(form.getEmpId());

            // Manager KHÔNG được đổi dept/job/user/directManager
            form.setDeptId(current.getDeptId());
            form.setJobId(current.getJobId());
            form.setUserId(current.getUserId());
            form.setDirectManagerId(current.getDirectManagerId());

            employeeService.update(form);

            ra.addFlashAttribute("msg", "Updated employee information successfully.");
            return "redirect:/manager/org/employees/" + form.getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/manager/org/employees/" + form.getEmpId();
        }
    }
}