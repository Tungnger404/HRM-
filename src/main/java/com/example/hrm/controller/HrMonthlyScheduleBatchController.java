package com.example.hrm.controller;

import com.example.hrm.dto.MonthlyScheduleBatchFormDTO;
import com.example.hrm.service.MonthlyScheduleBatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/schedules/monthly")
public class HrMonthlyScheduleBatchController {

    private final MonthlyScheduleBatchService service;

    public HrMonthlyScheduleBatchController(MonthlyScheduleBatchService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("batches", service.findAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new MonthlyScheduleBatchFormDTO());
        }
        return "hr/monthly-schedule-batches";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("form") MonthlyScheduleBatchFormDTO form,
                         RedirectAttributes ra) {
        try {
            service.create(form);
            ra.addFlashAttribute("msg", "Created monthly schedule batch successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
            ra.addFlashAttribute("form", form);
        }
        return "redirect:/hr/schedules/monthly";
    }
}
