package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.JobPosition;
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

@Controller
@RequestMapping("/manager/promotion")
@RequiredArgsConstructor
public class ManagerPromotionController {

    private final PromotionService promotionService;
    private final EmployeeRepository employeeRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserRepository userRepository;

    private Employee getCurrentManager(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }

    @GetMapping("")
    public String showPromotionRecommendations(Model model, Authentication auth,
                                               @ModelAttribute("msg") String msg,
                                               @ModelAttribute("err") String err) {
        Employee currentManager = getCurrentManager(auth);

        List<Map<String, Object>> eligibleEmployees =
                promotionService.getEligibleEmployeesForPromotion(currentManager.getEmpId());

        List<JobPosition> allPositions = jobPositionRepository.findAll();

        model.addAttribute("employees", eligibleEmployees);
        model.addAttribute("positions", allPositions);
        model.addAttribute("pageTitle", "Promotion Recommendations");

        return "manager/promotion-recommendations";
    }

    @GetMapping("/review/{empId}")
    @ResponseBody
    public Map<String, Object> getEmployeeEvaluationHistory(@PathVariable Integer empId,
                                                            Authentication auth) {
        Employee currentManager = getCurrentManager(auth);

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
            throw new RuntimeException("You cannot review this employee");
        }

        return promotionService.getEmployeeEvaluationHistory(empId);
    }

    @PostMapping("/submit")
    public String submitPromotionRequest(@RequestParam Integer empId,
                                         @RequestParam Integer proposedPositionId,
                                         @RequestParam String reason,
                                         Authentication auth,
                                         RedirectAttributes ra) {
        try {
            Employee currentManager = getCurrentManager(auth);

            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            if (!currentManager.getEmpId().equals(employee.getDirectManagerId())) {
                throw new RuntimeException("You cannot submit promotion for this employee");
            }

            promotionService.submitPromotionRequest(
                    empId,
                    proposedPositionId,
                    reason,
                    currentManager.getEmpId()
            );

            ra.addFlashAttribute("msg", "Promotion request submitted successfully to HR");
            return "redirect:/manager/promotion";

        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error: " + e.getMessage());
            return "redirect:/manager/promotion";
        }
    }

    @GetMapping("/my-requests")
    public String showMyRequests(Model model, Authentication auth,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err) {
        Employee currentManager = getCurrentManager(auth);

        model.addAttribute("requests", promotionService.getMyPromotionRequests(currentManager.getEmpId()));
        model.addAttribute("pageTitle", "My Promotion Requests");

        return "manager/my-promotion-requests";
    }
}