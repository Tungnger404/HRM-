package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.entity.Employee;
import com.example.hrm.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // LIST
    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("employees", service.list(q));
        return "employees/employee_list";
    }

    // SHOW CREATE FORM
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new EmployeeAdd());
        return "employees/employee_add";
    }

    // CREATE
    @PostMapping("/create")
    public String create(@ModelAttribute("form") EmployeeAdd form, RedirectAttributes ra) {
        try {
            Employee created = service.create(form);
            ra.addFlashAttribute("msg", "Created employee successfully!");
            return "redirect:/employees/" + created.getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/employees/new";
        }
    }

    // DETAIL (VIEW + EDIT)
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Employee e = service.getById(id);
        model.addAttribute("employee", e);
        model.addAttribute("form", service.toForm(e)); // nếu bạn muốn bind theo form
        return "employees/employee_detail";
    }

    // UPDATE
    @PostMapping("/save")
    public String save(@ModelAttribute("form") EmployeeAdd form, RedirectAttributes ra) {
        try {
            service.update(form);
            ra.addFlashAttribute("msg", "Saved changes!");
            return "redirect:/employees/" + form.getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            // nếu empId null thì quay về list
            return "redirect:/employees";
        }
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("msg", "Deleted employee!");
            return "redirect:/employees";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/employees/" + id;
        }
    }
}
