package com.example.hrm.controller;

import com.example.hrm.dto.ShiftAssignmentFormDTO;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.MonthlyScheduleBatchRepository;
import com.example.hrm.repository.ShiftTemplateRepository;
import com.example.hrm.service.ShiftAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/schedules/assignments")
public class HrShiftAssignmentController {

    private final ShiftAssignmentService service;
    private final MonthlyScheduleBatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    public HrShiftAssignmentController(ShiftAssignmentService service,
                                       MonthlyScheduleBatchRepository batchRepository,
                                       EmployeeRepository employeeRepository,
                                       ShiftTemplateRepository shiftTemplateRepository) {
        this.service = service;
        this.batchRepository = batchRepository;
        this.employeeRepository = employeeRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
    }

    @GetMapping
    public String page(@RequestParam(required = false) Long batchId, Model model) {
        model.addAttribute("batches", batchRepository.findAllByOrderByScheduleMonthDescBatchIdDesc());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("shifts", shiftTemplateRepository.findAll());
        model.addAttribute("types", new String[]{"WORK", "OFF", "LEAVE", "HOLIDAY"});
        model.addAttribute("selectedBatchId", batchId);

        if (!model.containsAttribute("form")) {
            ShiftAssignmentFormDTO form = new ShiftAssignmentFormDTO();
            form.setBatchId(batchId);
            form.setAssignmentType("WORK");
            model.addAttribute("form", form);
        }

        if (batchId != null) {
            model.addAttribute("assignments", service.listByBatch(batchId));
        }

        return "hr/shift-assignments";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") ShiftAssignmentFormDTO form, RedirectAttributes ra) {
        try {
            service.createOrUpdate(form);
            ra.addFlashAttribute("msg", "Saved assignment successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
            ra.addFlashAttribute("form", form);
        }
        return "redirect:/hr/schedules/assignments?batchId=" + form.getBatchId();
    }
}