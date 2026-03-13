package com.example.hrm.controller;

import com.example.hrm.entity.ShiftTemplate;
import com.example.hrm.service.ShiftTemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hr/shifts")
public class HrShiftTemplateController {

    private final ShiftTemplateService service;

    public HrShiftTemplateController(ShiftTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("shifts", service.findAll());
        model.addAttribute("shift", new ShiftTemplate());
        return "hr/shift-list";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute ShiftTemplate shift) {
        service.create(shift);
        return "redirect:/hr/shifts";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id, @ModelAttribute ShiftTemplate shift) {
        service.update(id, shift);
        return "redirect:/hr/shifts";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Integer id) {
        service.deactivate(id);
        return "redirect:/hr/shifts";
    }
}