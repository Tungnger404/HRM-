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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payroll")
public class ManagerPayrollController {

    private final PayrollService payrollService;
    private final CurrentEmployeeService currentEmployeeService;

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

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr)
            managerEmpId = null;

        try {
            model.addAttribute("p", payrollService.getPayslipDetailForManager(managerEmpId, payslipId));
            return "manager/payslip-detail";
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            model.addAttribute("err", ex.getMessage());
            return "error/403"; // hoặc trang lỗi bạn đang dùng
        }
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
                       @RequestParam(value = "payslipIds", required = false) List<Integer> payslipIds,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       Principal principal,
                       RedirectAttributes ra) {

        String redirectBack = "redirect:/manager/payroll/payslips"
                + ((q != null && !q.isBlank()) ? "?q=" + q : "")
                + ((status != null && !status.isBlank())
                ? ((q != null && !q.isBlank()) ? "&" : "?") + "status=" + status
                : "");

        if (payslipIds == null || payslipIds.isEmpty()) {
            ra.addFlashAttribute("msgType", "warning");
            ra.addFlashAttribute("msg", "Bạn chưa chọn dòng nào.");
            return redirectBack;
        }

        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();

        List<Integer> ids = payslipIds.stream().filter(Objects::nonNull).distinct().toList();

        int ok = 0, fail = 0;

        try {
            if ("approve".equalsIgnoreCase(action)) {
                // approve theo batch của các payslip đã chọn
                List<Integer> batchIds = payrollService.findBatchIdsByPayslipIds(ids);
                batchIds = batchIds.stream().filter(Objects::nonNull).distinct().toList();

                List<Integer> approvedIds = new ArrayList<>();

                for (Integer batchId : batchIds) {
                    try {
                        try {
                            payrollService.approveBatch(batchId, approverEmpId);
                        } catch (IllegalStateException ex) {
                            payrollService.submitBatchForApproval(batchId);
                            payrollService.approveBatch(batchId, approverEmpId);
                        }
                        ok++;
                        approvedIds.add(batchId);
                    } catch (Exception e) {
                        fail++;
                    }
                }

                if (!approvedIds.isEmpty()) {
                    String idsParam = approvedIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
                    return "redirect:/bank/portal?batchIds=" + idsParam + "&auto=1";
                }
            } else if ("reject".equalsIgnoreCase(action)) {
                // reject 1 dòng = xoá đúng payslip
                for (Integer payslipId : ids) {
                    try {
                        payrollService.rejectPayslip(payslipId);
                        ok++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
            } else {
                ra.addFlashAttribute("msgType", "danger");
                ra.addFlashAttribute("msg", "Action không hợp lệ.");
                return redirectBack;
            }

        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", "Lỗi xử lý bulk.");
            return redirectBack;
        }

        String msgType = (fail == 0) ? "success" : (ok == 0 ? "danger" : "warning");
        ra.addFlashAttribute("msgType", msgType);
        ra.addFlashAttribute("msg", "Done: ok=" + ok + ", fail=" + fail);
        return redirectBack;
    }

    @PostMapping(value = "/payslips/bulk-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> bulkJson(@RequestParam("action") String action,
                                        @RequestParam(value = "payslipIds", required = false) List<Integer> payslipIds,
                                        Principal principal) {

        Map<String, Object> res = new HashMap<>();

        if (payslipIds == null || payslipIds.isEmpty()) {
            res.put("ok", 0);
            res.put("fail", 0);
            res.put("msgType", "warning");
            res.put("msg", "Bạn chưa chọn dòng nào.");
            res.put("processedIds", List.of());
            return res;
        }

        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();

        List<Integer> ids = payslipIds.stream().filter(Objects::nonNull).distinct().toList();

        int ok = 0, fail = 0;
        List<Integer> processed = new ArrayList<>();

        if ("approve".equalsIgnoreCase(action)) {
            // approve theo batch của các payslip đã chọn
            List<Integer> batchIds = payrollService.findBatchIdsByPayslipIds(ids);
            batchIds = batchIds.stream().filter(Objects::nonNull).distinct().toList();

            List<Integer> approvedBatchIds = new ArrayList<>();

            for (Integer batchId : batchIds) {
                try {
                    try {
                        payrollService.approveBatch(batchId, approverEmpId);
                    } catch (IllegalStateException ex) {
                        payrollService.submitBatchForApproval(batchId);
                        payrollService.approveBatch(batchId, approverEmpId);
                    }
                    ok++;
                    approvedBatchIds.add(batchId);
                } catch (Exception e) {
                    fail++;
                }
            }

            res.put("ok", ok);
            res.put("fail", fail);
            res.put("processedIds", List.of()); // UI không cần xoá vì sẽ redirect

            String msgType = (fail > 0) ? "warning" : "success";
            res.put("msgType", msgType);
            res.put("msg", "Done");

            if (!approvedBatchIds.isEmpty()) {
                String idsParam = approvedBatchIds.stream().map(String::valueOf)
                        .reduce((a, b) -> a + "," + b).orElse("");
                res.put("redirectUrl", "/bank/portal?batchIds=" + idsParam + "&auto=1");
            }
            return res;
        }

        if ("reject".equalsIgnoreCase(action)) {
            for (Integer payslipId : ids) {
                try {
                    payrollService.rejectPayslip(payslipId);
                    ok++;
                    processed.add(payslipId); // ✅ trả về để UI đổi trạng thái
                } catch (Exception e) {
                    fail++;
                }
            }
            res.put("ok", ok);
            res.put("fail", fail);
            res.put("processedIds", processed);
            res.put("msgType", (fail > 0) ? "warning" : "success");
            res.put("msg", "Rejected (status updated)");
            return res;
        }

        res.put("ok", 0);
        res.put("fail", ids.size());
        res.put("processedIds", List.of());
        res.put("msgType", "danger");
        res.put("msg", "Action không hợp lệ.");
        return res;
    }

    //update nut sua
    @PostMapping(value = "/payslips/{payslipId}/salary", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updatePayslipSalary(@PathVariable Integer payslipId,
                                                   @RequestParam("baseSalary") BigDecimal baseSalary,
                                                   Principal principal,
                                                   Authentication authentication) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) managerEmpId = null;

        PayrollService.SalaryUpdateResult r = payrollService.updatePayslipBaseSalary(managerEmpId, payslipId, baseSalary);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", 1);
        res.put("payslipId", payslipId);
        res.put("baseSalary", r.baseSalary());
        res.put("netSalary", r.netSalary());
        res.put("slipStatus", r.slipStatus());
        return res;
    }
}