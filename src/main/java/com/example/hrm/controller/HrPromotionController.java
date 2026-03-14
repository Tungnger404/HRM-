package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.PromotionRequest;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hr/promotions")
@RequiredArgsConstructor
public class HrPromotionController {

    private final PromotionService promotionService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    public String showPendingPromotions(Model model,
                                       @ModelAttribute("msg") String msg,
                                       @ModelAttribute("err") String err) {
        List<PromotionRequest> pendingRequests = promotionService.getPendingPromotionRequests();
        
        model.addAttribute("requests", pendingRequests);
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
