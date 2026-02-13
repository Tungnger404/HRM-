package com.example.hrm.controller;

import com.example.hrm.dto.*;
import com.example.hrm.service.JobDescriptionService;
import com.example.hrm.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("jobs", jobRepository.findAll()); // dropdown
        return "job-description/create";
    }

    // ================= CREATE =================
    @PostMapping("/create")
    public String create(@ModelAttribute JobDescriptionCreateDTO dto) {
        service.create(dto);
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

        model.addAttribute("dto", dto);
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
                               @PathVariable String status) {

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
