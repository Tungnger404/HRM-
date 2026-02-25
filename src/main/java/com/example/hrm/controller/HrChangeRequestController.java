package com.example.hrm.controller;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeChangeRequest;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.EmployeeChangeRequestRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserAccountRepository;
import com.example.hrm.service.EmployeeChangeRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/hr/change-requests")
public class HrChangeRequestController {

    private final EmployeeChangeRequestService changeReqService;
    private final EmployeeChangeRequestRepository reqRepo;
    private final EmployeeRepository empRepo;
    private final UserAccountRepository userRepo;

    public HrChangeRequestController(EmployeeChangeRequestService changeReqService,
                                     EmployeeChangeRequestRepository reqRepo,
                                     EmployeeRepository empRepo,
                                     UserAccountRepository userRepo) {
        this.changeReqService = changeReqService;
        this.reqRepo = reqRepo;
        this.empRepo = empRepo;
        this.userRepo = userRepo;
    }

    private Map<String, String> fieldOptions() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("phone", "Phone");
        m.put("address", "Address");
        m.put("identityCard", "Identity Card");
        m.put("taxCode", "Tax Code");
        return m;
    }

    private Integer currentUserId(Principal principal) {
        if (principal == null || principal.getName() == null) throw new IllegalStateException("Not authenticated");
        UserAccount u = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));
        return u.getId();
    }

    // LIST (pending)
    @GetMapping
    public String pending(Model model) {
        model.addAttribute("requests", changeReqService.pending());
        model.addAttribute("fieldOptions", fieldOptions());
        // Dùng lại view của manager cho nhanh (đỡ copy file)
        return "hr/change-request-pending";
        // Nếu bạn muốn tách view HR riêng thì đổi thành: "hr/change-request-pending"
    }

    // DETAIL
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        EmployeeChangeRequest r = reqRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
        Employee e = empRepo.findById(r.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + r.getEmployeeId()));

        model.addAttribute("r", r);
        model.addAttribute("employee", e);
        model.addAttribute("fieldOptions", fieldOptions());
        return "hr/change-request-detail";
        // Nếu tách view HR riêng: "hr/change-request-detail"
    }

    // APPROVE
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id,
                          @RequestParam(value = "decisionNote", required = false) String decisionNote,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            changeReqService.approve(id, currentUserId(principal), decisionNote);
            ra.addFlashAttribute("msg", "Approved request #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/change-requests";
    }

    // REJECT
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id,
                         @RequestParam(value = "decisionNote", required = false) String decisionNote,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            changeReqService.reject(id, currentUserId(principal), decisionNote);
            ra.addFlashAttribute("msg", "Rejected request #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/change-requests";
    }
}