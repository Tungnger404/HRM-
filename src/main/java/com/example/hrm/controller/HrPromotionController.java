package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.entity.PromotionRequest;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hr/promotions")
@RequiredArgsConstructor
public class HrPromotionController {

    private final PromotionService promotionService;
    private final EmployeeRepository employeeRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    public String showPendingPromotions(Model model,
                                       @ModelAttribute("msg") String msg,
                                       @ModelAttribute("err") String err) {
        List<PromotionRequest> pendingRequests = promotionService.getPendingPromotionRequests();
        
        Set<Integer> employeeIds = pendingRequests.stream().map(PromotionRequest::getEmpId).collect(Collectors.toSet());
        Set<Integer> requesterIds = pendingRequests.stream().map(PromotionRequest::getRequestedBy).collect(Collectors.toSet());
        Set<Integer> positionIds = pendingRequests.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getCurrentPositionId(), r.getProposedPositionId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Integer, String> employeeNames = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));
        Map<Integer, String> requesterNames = employeeRepository.findAllById(requesterIds).stream()
                .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));
        Map<Integer, String> positionTitles = jobPositionRepository.findAllById(positionIds).stream()
                .collect(Collectors.toMap(JobPosition::getJobId, JobPosition::getTitle));
         
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("employeeNames", employeeNames);
        model.addAttribute("requesterNames", requesterNames);
        model.addAttribute("positionTitles", positionTitles);
        model.addAttribute("pageTitle", "Pending Promotion Requests");
        
        return "hr/promotion-pending";
    }

    @PostMapping("/{requestId}/approve")
    public String approvePromotion(
            @PathVariable Integer requestId,
            @RequestParam(required = false) String hrComment,
            Authentication auth,
            RedirectAttributes ra) {
        
        try {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Employee hrEmployee = employeeRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("HR employee not found"));
            
            promotionService.approvePromotionRequest(requestId, hrEmployee.getEmpId(), hrComment);
            
            ra.addFlashAttribute("msg", "Promotion request approved successfully");
            return "redirect:/hr/promotions/pending";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/promotions/pending";
        }
    }

    @PostMapping("/{requestId}/reject")
    public String rejectPromotion(
            @PathVariable Integer requestId,
            @RequestParam String hrComment,
            Authentication auth,
            RedirectAttributes ra) {
        
        try {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Employee hrEmployee = employeeRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("HR employee not found"));
            
            promotionService.rejectPromotionRequest(requestId, hrEmployee.getEmpId(), hrComment);
            
            ra.addFlashAttribute("msg", "Promotion request rejected");
            return "redirect:/hr/promotions/pending";
            
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/hr/promotions/pending";
        }
    }
}
