package com.example.hrm.controller;

import com.example.hrm.dto.BenefitUpsertDTO;
import com.example.hrm.entity.Benefit;
import com.example.hrm.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor    
@RequestMapping("/manager/payroll/benefits")
public class ManagerBenefitController {

    private final BenefitService benefitService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "type", required = false) String type,
                       Model model) {

        List<Benefit> list = benefitService.list(q, type);

        model.addAttribute("benefits", list);
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("typeOptions", List.of("", "INCOME", "DEDUCTION"));

        model.addAttribute("f", new BenefitUpsertDTO()); // form create
        return "manager/benefit-list";
    }

    @PostMapping
    public String create(@ModelAttribute("f") BenefitUpsertDTO f, RedirectAttributes ra) {
        benefitService.create(f);
        ra.addFlashAttribute("msg", "Đã tạo phúc lợi (benefit).");
        return "redirect:/manager/payroll/benefits";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute BenefitUpsertDTO f,
                         RedirectAttributes ra) {

        benefitService.update(id, f);
        ra.addFlashAttribute("msg", "Đã cập nhật phúc lợi.");
        return "redirect:/manager/payroll/benefits";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        benefitService.delete(id);
        ra.addFlashAttribute("msg", "Đã xóa phúc lợi.");
        return "redirect:/manager/payroll/benefits";
    }
}