package com.example.hrm.controller;

import com.example.hrm.dto.BatchInterviewDTO;
import com.example.hrm.dto.CandidateEvaluateDTO;
import com.example.hrm.dto.CandidateListDTO;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.service.CandidateService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/hr/candidates")
public class HRCandidateController {

    private final CandidateService service;

    public HRCandidateController(CandidateService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam("postingId") Integer postingId,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {


        CandidateStatus statusEnum = null;

        if (status != null && !status.isBlank()) {
            try {
                statusEnum = CandidateStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                statusEnum = null;
            }
        }

        List<CandidateListDTO> candidates =
                service.getCandidates(postingId, statusEnum, keyword);

        model.addAttribute("candidates", candidates);
        model.addAttribute("postingId", postingId);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", CandidateStatus.values());

        return "candidate/list";
    }


    @GetMapping("/evaluate/{id}")
    public String evaluatePage(@PathVariable("id") Integer id,
                               Model model) {

        CandidateEvaluateDTO dto = service.getEvaluateDTO(id);

        if (dto == null) {
            return "redirect:/hr/candidates";
        }

        model.addAttribute("candidate", dto);

        return "candidate/evaluate";
    }


    @PostMapping("/evaluate")
    public String evaluate(@Valid @ModelAttribute("candidate") CandidateEvaluateDTO dto,
                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {
            return "candidate/evaluate";
        }

        service.evaluate(dto);

        return "redirect:/hr/candidates?postingId=" + dto.getPostingId();
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id,
                         @RequestParam Integer postingId,
                         Model model) {

        Candidate candidate = service.findById(id);

        if (candidate == null) {
            return "redirect:/hr/candidates?postingId=" + postingId;
        }

        model.addAttribute("candidate", candidate);
        model.addAttribute("postingId", postingId);

        return "candidate/detail";
    }
    @PostMapping("/batch-interview")
    public String scheduleBatchInterview(
            @ModelAttribute BatchInterviewDTO dto,
            @RequestParam Integer postingId) {

        service.scheduleBatchInterview(dto);

        return "redirect:/hr/candidates?postingId=" + postingId;
    }
}