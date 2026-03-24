package com.example.hrm.controller;

import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.Interview;
import com.example.hrm.entity.InterviewResult;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.InterviewRepository;
import com.example.hrm.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/interview")
public class InterviewController {

    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewService interviewService;

    @GetMapping("")
    public String interviewList(Model model) {

        List<Interview> interviews = interviewRepository.findAll();
        model.addAttribute("interviews", interviews);

        return "interview/list";
    }

    @GetMapping("/evaluation/{id}")
    public String viewEvaluation(@PathVariable Integer id,
                                 Model model) {

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        List<Interview> interviews =
                interviewRepository.findByCandidateCandidateId(id);

        model.addAttribute("candidate", candidate);
        model.addAttribute("interviews", interviews);

        return "interview/evaluation";
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/submit-hr")
    public String submitHr(@RequestParam Integer candidateId,
                           @RequestParam Integer score,
                           @RequestParam String feedback,
                           @RequestParam InterviewResult result,
                           RedirectAttributes redirectAttributes) {
        try {
            interviewService.submitEvaluation(
                    candidateId,
                    1,
                    score,
                    feedback,
                    result
            );
            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "HR Evaluation submitted successfully!");
        } catch (RuntimeException e) {
            // Bắt lỗi từ Service (lỗi check status != INTERVIEW_SCHEDULED)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/interview/evaluation/" + candidateId;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/submit-manager")
    public String submitManager(@RequestParam Integer candidateId,
                                @RequestParam Integer score,
                                @RequestParam String feedback,
                                @RequestParam InterviewResult result,
                                RedirectAttributes redirectAttributes) {
        try {
            interviewService.submitEvaluation(
                    candidateId,
                    2,
                    score,
                    feedback,
                    result
            );
            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Manager Evaluation submitted successfully!");
        } catch (RuntimeException e) {
            // Bắt lỗi nếu cố tình submit lại lần nữa
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/interview/evaluation/" + candidateId;
    }
}