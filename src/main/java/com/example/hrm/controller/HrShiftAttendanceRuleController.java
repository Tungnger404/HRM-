package com.example.hrm.controller;

import com.example.hrm.service.ShiftAttendanceRuleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hr/shift-rules")
public class HrShiftAttendanceRuleController {

    private final ShiftAttendanceRuleService service;

    public HrShiftAttendanceRuleController(ShiftAttendanceRuleService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rules", service.findAll());
        return "hr/shift-rule-list";
    }

    @PostMapping("/upsert")
    public String upsert(@RequestParam String shiftCode,
                         @RequestParam Integer earlyCheckinMinutes,
                         @RequestParam Integer lateThresholdMinutes,
                         @RequestParam(defaultValue = "true") Boolean isActive) {
        service.upsertByShiftCode(shiftCode, earlyCheckinMinutes, lateThresholdMinutes, isActive);
        return "redirect:/hr/shift-rules";
    }
}