package com.example.hrm.controller;

import com.example.hrm.dto.JobPostingCreateDTO;
import com.example.hrm.entity.JobDescriptionStatus;
import com.example.hrm.entity.JobPosting;
import com.example.hrm.entity.RecruitmentRequestStatus;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.JobDescriptionRepository;
import com.example.hrm.repository.RecruitmentRequestRepository;
import com.example.hrm.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hr/job-posting")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService service;
    private final RecruitmentRequestRepository reqRepo;
    private final JobDescriptionRepository jdRepo;

    // ✅ NEW
    private final CandidateRepository candidateRepository;

    // ================= LIST =================
    @GetMapping
    public String list(Model model) {

        List<JobPosting> list = service.getAll();

        // 🔥 Map số lượng candidate cho từng job
        list.forEach(jp -> {
            long count = candidateRepository
                    .countByJobPosting_PostingId(jp.getPostingId());
            jp.setCandidateCount(count);
        });

        model.addAttribute("list", list);

        return "job-posting/list";
    }

    // ================= CREATE FORM =================
    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("dto", new JobPostingCreateDTO());

        model.addAttribute("requests",
                reqRepo.findByStatus(RecruitmentRequestStatus.APPROVED));

        model.addAttribute("descriptions",
                jdRepo.findByStatus(JobDescriptionStatus.ACTIVE));

        return "job-posting/create";
    }

    // ================= CREATE =================
    @PostMapping("/create")
    public String create(@ModelAttribute JobPostingCreateDTO dto, RedirectAttributes ra) {
        // 1. Chặn logic ngày tháng ngay lập tức
        if (dto.getPublishDate() != null && dto.getExpiryDate() != null) {
            if (dto.getExpiryDate().isBefore(dto.getPublishDate())) {
                ra.addFlashAttribute("err", "Ngày hết hạn không được trước ngày đăng tin!");
                return "redirect:/hr/job-posting/create";
            }
        }

        try {
            service.create(dto);
            return "redirect:/hr/job-posting?success";
        } catch (DataIntegrityViolationException e) {
            // Đây là nơi bắt lỗi 547 từ SQL Server
            ra.addFlashAttribute("err", "Lỗi dữ liệu: Trạng thái không hợp lệ với thời gian đăng tin!");
            return "redirect:/hr/job-posting/create";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi hệ thống không xác định.");
            return "redirect:/hr/job-posting/create";
        }
    }

    // ================= CHANGE STATUS =================
    @GetMapping("/status/{id}/{status}")
    public String changeStatus(@PathVariable Integer id,
                               @PathVariable String status) {
        service.changeStatus(id, status);
        return "redirect:/hr/job-posting";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/hr/job-posting";
    }
}