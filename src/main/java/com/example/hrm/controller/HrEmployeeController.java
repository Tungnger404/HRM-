package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Employee;
import com.example.hrm.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/employees")
public class HrEmployeeController {

    private final EmployeeService employeeService;

    public HrEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Profile Employee Management: LIST + SEARCH/FILTER
    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       Model model,
                       @ModelAttribute("msg") String msg,
                       @ModelAttribute("err") String err) {

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("employees", employeeService.list(q, status));

        return "hr/employee_list"; // -> templates/hr/employee_list.html
    }

    // Employee General Information: DETAIL (view/edit)
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("err") String err) {
        Employee e = employeeService.getById(id);
        model.addAttribute("e", e);
        return "hr/employee_detail"; // -> templates/hr/employee_detail.html
    }

    // UPDATE
    @PostMapping("/save")
    public String save(@ModelAttribute EmployeeAdd form, RedirectAttributes ra) {
        try {
            employeeService.update(form);
            ra.addFlashAttribute("msg", "Saved changes!");
            return "redirect:/hr/employees/" + form.getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/employees/" + (form.getEmpId() == null ? "" : form.getEmpId());
        }
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            employeeService.delete(id);
            ra.addFlashAttribute("msg", "Deleted employee!");
            return "redirect:/hr/employees";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/employees/" + id;
        }
    }
}
