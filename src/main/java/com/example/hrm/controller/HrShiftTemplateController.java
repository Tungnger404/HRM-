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
    private final com.example.hrm.service.ShiftAttendanceRuleService ruleService;

    public HrShiftTemplateController(ShiftTemplateService service, com.example.hrm.service.ShiftAttendanceRuleService ruleService) {
        this.service = service;
        this.ruleService = ruleService;
    }

    @GetMapping
    public String list(Model model) {
        java.util.List<ShiftTemplate> allShifts = service.findAll();
        // filter out inactive shifts to simulate deletion, since we removed the Status column
        java.util.List<ShiftTemplate> shifts = allShifts.stream()
                .filter(ShiftTemplate::getIsActive)
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<Integer, Integer> ruleMap = new java.util.HashMap<>();
        for (ShiftTemplate s : shifts) {
            try {
                com.example.hrm.entity.ShiftAttendanceRule r = ruleService.getByShiftCode(s.getShiftCode());
                ruleMap.put(s.getShiftId(), r.getLateThresholdMinutes());
            } catch (Exception e) {
                ruleMap.put(s.getShiftId(), 0);
            }
        }
        model.addAttribute("shifts", shifts);
        model.addAttribute("ruleMap", ruleMap);
        model.addAttribute("shift", new ShiftTemplate());
        return "hr/shift-list";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute ShiftTemplate shift,
                         @RequestParam(required = false, defaultValue = "15") Integer lateThresholdMinutes) {
        service.create(shift);
        ruleService.upsertByShiftCode(shift.getShiftCode(), 30, lateThresholdMinutes, true);
        return "redirect:/hr/shifts";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id, @ModelAttribute ShiftTemplate shift,
                         @RequestParam(required = false, defaultValue = "15") Integer lateThresholdMinutes) {
        service.update(id, shift);
        ruleService.upsertByShiftCode(shift.getShiftCode(), 30, lateThresholdMinutes, true);
        return "redirect:/hr/shifts";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Integer id) {
        service.deactivate(id);
        return "redirect:/hr/shifts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        try {
            service.deactivate(id);
            redirectAttrs.addFlashAttribute("success", "Shift deleted successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error removing shift.");
        }
        return "redirect:/hr/shifts";
    }
}