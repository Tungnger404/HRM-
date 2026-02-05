package com.example.hrm.controller.manager;

import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payroll")
public class ManagerPayrollController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService; // ✅ THÊM DÒNG NÀY

    // DEMO: bạn thay bằng current manager empId từ Security sau
    private Integer demoManagerEmpId() { return 2; }

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
}
