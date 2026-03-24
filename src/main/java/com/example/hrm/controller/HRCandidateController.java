package com.example.hrm.controller;

import com.example.hrm.dto.BatchInterviewDTO;
import com.example.hrm.dto.CandidateEvaluateDTO;
import com.example.hrm.dto.CandidateListDTO;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;
import com.example.hrm.service.CandidateService;

import com.example.hrm.service.InterviewService;
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
    private final InterviewService interviewService;

    public HRCandidateController(CandidateService service,
                                 InterviewService interviewService) {
        this.service = service;
        this.interviewService = interviewService;
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

    @PostMapping("/evaluate")
    public String evaluate(@Valid @ModelAttribute("candidate") CandidateEvaluateDTO dto,
                           BindingResult result,
                           Model model) {

        if (!result.hasFieldErrors("score")) {
            if ("pass".equalsIgnoreCase(dto.getAction()) && dto.getScore() < 50) {
                result.rejectValue("score", "error.score", "Điểm dưới 50 không thể PASS ứng viên.");
            } else if ("reject".equalsIgnoreCase(dto.getAction()) && dto.getScore() >= 50) {
                result.rejectValue("score", "error.score", "Ứng viên đạt từ 50 điểm trở lên không nên bị Reject.");
            }
        }


        if (result.hasErrors()) {
            Candidate candidate = service.findById(dto.getId());
            model.addAttribute("candidate", candidate);
            model.addAttribute("postingId", dto.getPostingId());
            return "candidate/detail";
        }
        service.evaluate(dto);

        return "redirect:/hr/candidates/detail/" + dto.getId() + "?postingId=" + dto.getPostingId();
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