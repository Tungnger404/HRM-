package com.example.hrm.controller;

import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payroll")
public class ManagerPayrollController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService; // ✅ THÊM DÒNG NÀY

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
    public String approve(@PathVariable Integer batchId) {
        payrollService.approveBatch(batchId, demoManagerEmpId());
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
    public String payslipDetail(@PathVariable Integer payslipId, Model model, Principal principal) {
        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId(); // ✅ giờ dùng được
        model.addAttribute("p", payrollService.getPayslipDetailForManager(managerEmpId, payslipId));
        return "manager/payslip-detail";
    }

    @GetMapping("/payslips")
    public String payrollList(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "status", required = false) String status,
                              Model model,
                              Principal principal,
                              Authentication authentication) {

        // Manager thường chỉ thấy nhân viên mình quản lý
        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();

        // Nếu HR/ADMIN muốn thấy tất cả thì bật đoạn này:
        boolean isAdminOrHr = authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR")
        );
        if (isAdminOrHr)
            managerEmpId = null;

        model.addAttribute("rows", payrollService.listPayrollRowsForManager(managerEmpId, q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statusOptions", List.of("", "DRAFT", "PENDING_APPROVAL", "APPROVED", "PAID"));

        return "manager/payroll-list"; // -> /WEB-INF/views/manager/payroll-list.jsp
    }

    @PostMapping("/payslips/bulk")
    public String bulk(@RequestParam("action") String action,
                       @RequestParam(value = "batchIds", required = false) List<Integer> batchIds,
                       Principal principal,
                       RedirectAttributes ra) {

        if (batchIds == null || batchIds.isEmpty()) {
            ra.addFlashAttribute("msg", "Bạn chưa chọn dòng nào.");
            return "redirect:/manager/payroll/payslips";
        }

        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();

        // distinct batch ids
        List<Integer> ids = batchIds.stream().distinct().toList();

        int ok = 0, fail = 0;

        for (Integer batchId : ids) {
            try {
                if ("approve".equalsIgnoreCase(action)) {
                    // nếu đang DRAFT thì submit rồi approve (để demo chạy mượt)
                    try {
                        payrollService.approveBatch(batchId, approverEmpId);
                    } catch (IllegalStateException ex) {
                        payrollService.submitBatchForApproval(batchId);
                        payrollService.approveBatch(batchId, approverEmpId);
                    }
                    ok++;
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

        ra.addFlashAttribute("msg", "Done: ok=" + ok + ", fail=" + fail);
        return "redirect:/manager/payroll/payslips";
    }


}
