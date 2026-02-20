package com.example.hrm.controller;

import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payroll")
public class ManagerPayrollController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService;

    // DEMO: bạn thay bằng current manager empId từ Security sau
    private Integer demoManagerEmpId() {
        return 2;
    }

    @GetMapping("/periods")
    public String periods(Model model) {
        model.addAttribute("periods", payrollService.listPayrollPeriods());
        return "manager/payroll-period-list";
    }

    @GetMapping("/periods/{periodId}/batches")
    public String batches(@PathVariable Integer periodId, Model model) {
        model.addAttribute("periodId", periodId);
        model.addAttribute("batches", payrollService.listBatchesByPeriod(periodId));
        return "manager/payroll-batch-list";
    }

    @PostMapping("/periods/{periodId}/generate")
    public String generate(@PathVariable Integer periodId) {
        Integer batchId = payrollService.generatePayrollDraft(periodId, demoManagerEmpId());
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @GetMapping("/batches/{batchId}")
    public String batchDetail(@PathVariable Integer batchId, Model model) {
        model.addAttribute("b", payrollService.viewBatchDetail(batchId));
        return "manager/payroll-detail";
    }

    @PostMapping("/batches/{batchId}/submit")
    public String submitForApproval(@PathVariable Integer batchId) {
        payrollService.submitBatchForApproval(batchId);
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @PostMapping("/batches/{batchId}/approve")
    public String approve(@PathVariable Integer batchId, Principal principal) {
        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();
        payrollService.approveBatch(batchId, approverEmpId);
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @PostMapping("/batches/{batchId}/reject")
    public String reject(@PathVariable Integer batchId) {
        payrollService.rejectBatch(batchId);
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @GetMapping("/batches/{batchId}/export")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Integer batchId) {
        byte[] xlsx = payrollService.exportBatchExcel(batchId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-batch-" + batchId + ".xlsx")
                .body(xlsx);
    }

    @GetMapping("/payslips/{payslipId}")
    public String payslipDetail(@PathVariable Integer payslipId,
                                Model model,
                                Principal principal,
                                Authentication authentication) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();

        // Allow Admin/HR to view all
        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) {
            managerEmpId = null;
        }

        model.addAttribute("p", payrollService.getPayslipDetailForManager(managerEmpId, payslipId));
        return "manager/payslip-detail";
    }

    @GetMapping("/payslips")
    public String payrollList(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "status", required = false) String status,
                              Model model,
                              Principal principal,
                              Authentication authentication) {

        Integer managerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHr) {
            managerEmpId = null;
        } else {
            managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        }

        model.addAttribute("rows", payrollService.listPayrollRowsForManager(managerEmpId, q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statusOptions", List.of("", "DRAFT", "PENDING_APPROVAL", "APPROVED", "PAID"));

        return "manager/payroll-list";
    }

    @PostMapping("/payslips/bulk")
    public String bulk(@RequestParam("action") String action,
                       @RequestParam(value = "batchIds", required = false) List<Integer> batchIds,
                       Principal principal,
                       RedirectAttributes ra) {

        // ✅ redirect về Pending để dòng xử lý xong biến mất
        String redirectPending = "redirect:/manager/payroll/payslips?status=PENDING_APPROVAL";

        if (batchIds == null || batchIds.isEmpty()) {
            ra.addFlashAttribute("msgType", "warning");
            ra.addFlashAttribute("msg", "Bạn chưa chọn dòng nào (chỉ chọn được trạng thái PENDING).");
            return redirectPending;
        }

        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();

        // distinct batch ids
        List<Integer> ids = batchIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        int ok = 0, fail = 0;
        List<Integer> approvedIds = new ArrayList<>();

        for (Integer batchId : ids) {
            try {
                if ("approve".equalsIgnoreCase(action)) {
                    try {
                        payrollService.approveBatch(batchId, approverEmpId);
                    } catch (IllegalStateException ex) {
                        // nếu đang DRAFT thì submit rồi approve
                        payrollService.submitBatchForApproval(batchId);
                        payrollService.approveBatch(batchId, approverEmpId);
                    }
                    ok++;
                    approvedIds.add(batchId);

                } else if ("reject".equalsIgnoreCase(action)) {
                    payrollService.rejectBatch(batchId);
                    ok++;

                } else {
                    fail++;
                }
            } catch (Exception e) {
                fail++;
            }
        }

        // ✅ approve xong -> sang bank portal và auto download excel
        if ("approve".equalsIgnoreCase(action) && !approvedIds.isEmpty()) {
            String idsParam = approvedIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            return "redirect:/bank/portal?batchIds=" + idsParam + "&auto=1";
        }

        // ✅ msg màu đúng theo ok/fail
        String msgType = (fail == 0) ? "success" : (ok == 0 ? "danger" : "warning");
        ra.addFlashAttribute("msgType", msgType);
        ra.addFlashAttribute("msg", "Done: ok=" + ok + ", fail=" + fail);

        // ✅ reject xong quay về Pending để batch vừa reject biến mất khỏi list Pending
        return redirectPending;
    }
}