package com.example.hrm.controller;

import com.example.hrm.dto.*;
import com.example.hrm.entity.JobDescriptionStatus;
import com.example.hrm.service.JobDescriptionService;
import com.example.hrm.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/hr/job-description")
@RequiredArgsConstructor
public class JobDescriptionController {

    private final JobDescriptionService service;
    private final JobPositionRepository jobRepository;

    // ================= LIST =================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.getAll());
        return "job-description/list";
    }

    // ================= CREATE FORM =================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dto", new JobDescriptionCreateDTO());
        model.addAttribute("jobs", jobRepository.findAll());
        return "job-description/create";
    }

    // ================= CREATE =================
    @PostMapping("/create")
    public String create(@ModelAttribute JobDescriptionCreateDTO dto,
                         Principal principal) {

        service.create(dto, principal);
        return "redirect:/hr/job-description?success";
    }

    // ================= DETAIL =================
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("jd", service.getById(id));
        return "job-description/detail";
    }

    // ================= EDIT FORM =================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {

        JobDescriptionResponseDTO response = service.getById(id);

        JobDescriptionUpdateDTO dto = new JobDescriptionUpdateDTO();
        dto.setSalaryRange(response.getSalaryRange());
        dto.setWorkingLocation(response.getWorkingLocation());
        dto.setStatus(response.getStatus());
        dto.setResponsibilities(response.getResponsibilities());
        dto.setRequirements(response.getRequirements());
        dto.setBenefits(response.getBenefits());

        model.addAttribute("dto", dto);
        model.addAttribute("statuses", JobDescriptionStatus.values());
        model.addAttribute("id", id);

        return "job-description/edit";
    }



    // ================= UPDATE =================
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute JobDescriptionUpdateDTO dto) {

        service.update(id, dto);
        return "redirect:/hr/job-description?updated";
    }

    // ================= CHANGE STATUS =================

    @GetMapping("/status/{id}/{status}")
    public String changeStatus(@PathVariable Integer id,
                               @PathVariable JobDescriptionStatus status) {

        service.changeStatus(id, status);
        return "redirect:/hr/job-description";
    }


    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        service.delete(id);
        return "redirect:/hr/job-description";
    }
}
