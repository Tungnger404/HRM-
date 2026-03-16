package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeSearchResultDTO;
import com.example.hrm.dto.PayrollBatchSummaryDTO;
import com.example.hrm.dto.PayrollPeriodSummaryDTO;
import com.example.hrm.dto.PayrollRowDTO;
import com.example.hrm.service.BenefitService;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollExportService;
import com.example.hrm.service.PayrollManagerService;
import com.example.hrm.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payroll")
public class ManagerPayrollController {

    private final PayrollManagerService payrollManagerService;
    private final PayrollExportService payrollExportService;
    private final CurrentEmployeeService currentEmployeeService;
    private final BenefitService benefitService;
    private final PayslipRepository payslipRepo;

    @GetMapping("/periods")
    public String periods(Model model) {
        model.addAttribute("periods", payrollManagerService.listPayrollPeriods());
        return "manager/payroll-period-list";
    }

    @PostMapping("/periods/create")
    public String createPeriod(@RequestParam("month") Integer month,
                               @RequestParam("year") Integer year,
                               RedirectAttributes ra) {
        try {
            payrollManagerService.createPayrollPeriod(month, year);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Tạo kỳ lương thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/manager/payroll/periods";
    }

    @PostMapping("/periods/create-current")
    public String createCurrentPeriod(RedirectAttributes ra) {
        try {
            LocalDate now = LocalDate.now();
            payrollManagerService.createPayrollPeriod(now.getMonthValue(), now.getYear());

            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã tạo kỳ lương tháng hiện tại.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/manager/payroll/periods";
    }

    @GetMapping("/periods/{periodId}/batches")
    public String batches(@PathVariable Integer periodId, Model model) {
        String periodName = payrollManagerService.listPayrollPeriods().stream()
                .filter(p -> p.getId().equals(periodId))
                .findFirst()
                .map(p -> String.format("%02d/%d", p.getMonth(), p.getYear()))
                .orElse("ID: " + periodId);

        model.addAttribute("periodId", periodId);
        model.addAttribute("periodName", periodName);
        model.addAttribute("batches", payrollManagerService.listBatchesByPeriod(periodId));
        return "manager/payroll-batch-list";
    }

    @PostMapping("/periods/{periodId}/delete")
    public String deletePeriod(@PathVariable Integer periodId, RedirectAttributes ra) {
        try {
            payrollManagerService.deletePeriod(periodId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã xóa kỳ lương thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/manager/payroll/periods";
    }

    @PostMapping("/periods/{periodId}/unlock")
    public String unlockPeriod(@PathVariable Integer periodId, RedirectAttributes ra) {
        try {
            payrollManagerService.unlockPeriod(periodId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã unlock kỳ lương. Có thể Generate Draft lại.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/manager/payroll/periods";
    }

    @GetMapping("/batches/{batchId}")
    public String batchDetail(@PathVariable Integer batchId, Model model) {
        model.addAttribute("b", payrollManagerService.viewBatchDetail(batchId));
        return "manager/payroll-detail";
    }

    @PostMapping("/batches/{batchId}/delete")
    public String deleteDraftBatch(@PathVariable Integer batchId, RedirectAttributes ra) {
        try {
            Integer periodId = payrollManagerService.deleteDraftBatch(batchId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã xóa batch DRAFT thành công.");
            return "redirect:/manager/payroll/periods/" + periodId + "/batches";
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/manager/payroll/batches/" + batchId;
        }
    }

    @PostMapping("/batches/{batchId}/submit")
    public String submitForApproval(@PathVariable Integer batchId) {
        payrollManagerService.submitBatchForApproval(batchId);
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @PostMapping("/batches/{batchId}/approve")
    public String approve(@PathVariable Integer batchId, Principal principal) {
        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();
        payrollManagerService.approveBatch(batchId, approverEmpId);
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @PostMapping("/batches/{batchId}/reject")
    public String reject(@PathVariable Integer batchId,
                         @RequestParam(value = "reason", required = false) String reason,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            Integer rejectedByEmpId = currentEmployeeService.requireEmployee(principal).getId();
            payrollManagerService.rejectBatch(batchId, rejectedByEmpId, reason);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Batch đã được reject.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/manager/payroll/batches/" + batchId;
    }

    @GetMapping("/batches/{batchId}/export")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Integer batchId) {
        byte[] xlsx = payrollExportService.exportBatchExcel(batchId);

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
        if (isAdminOrHr) managerEmpId = null;

        try {
            model.addAttribute("p", payrollManagerService.getPayslipDetailForManager(managerEmpId, payslipId));
            model.addAttribute("activeBenefits", benefitService.listActive());
            return "manager/payslip-detail";
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            model.addAttribute("err", ex.getMessage());
            return "error/403";
        }
    }

    @PostMapping("/payslips/{payslipId}/benefits/add")
    public String addBenefitToPayslip(@PathVariable Integer payslipId,
                                      @RequestParam("benefitId") Integer benefitId,
                                      Principal principal,
                                      Authentication authentication,
                                      RedirectAttributes ra) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) managerEmpId = null;

        try {
            payrollManagerService.addActiveBenefitToPayslip(managerEmpId, payslipId, benefitId);
            ra.addFlashAttribute("msg", "Đã thêm khoản lương vào payslip.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/manager/payroll/payslips/" + payslipId;
    }

    @PostMapping("/payslips/{id}/delete")
    public String deletePayslip(@PathVariable("id") Integer payslipId,
                                Principal principal,
                                Authentication authentication,
                                RedirectAttributes ra) {
        try {
            Integer managerEmpId = null;
            boolean isAdminOrHr = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_HR"));
            if (!isAdminOrHr) {
                managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
            }

            Integer periodId = null;
            com.example.hrm.entity.Payslip p = payslipRepo.findById(payslipId).orElse(null);
            if (p != null && p.getBatch() != null && p.getBatch().getPeriod() != null) {
                periodId = p.getBatch().getPeriod().getId();
            }

            payrollManagerService.deletePayslipFromBatch(managerEmpId, payslipId);
            ra.addFlashAttribute("msg", "Đã xóa phiếu lương khỏi bảng lương.");
            
            if (periodId != null) {
                return "redirect:/manager/payroll/payslips?periodId=" + periodId;
            }
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/manager/payroll/payslips";
    }

    @GetMapping("/payslips")
    public String payrollList(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "periodId", required = false) Integer periodId,
                              Model model,
                              Principal principal,
                              Authentication authentication) {

        Integer managerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHr) {
            managerEmpId = null;
        } else {
            managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        }

        List<PayrollPeriodSummaryDTO> periods = payrollManagerService.listPayrollPeriods();

        Integer effectivePeriodId = periodId;

        // Mới vào trang thì tự chọn kỳ tháng hiện tại
        if (effectivePeriodId == null) {
            LocalDate now = LocalDate.now();

            // Ưu tiên kỳ tháng hiện tại (dù có data hay không)
            effectivePeriodId = periods.stream()
                    .filter(p -> p.getMonth() != null && p.getYear() != null)
                    .filter(p -> p.getMonth().equals(now.getMonthValue()) && p.getYear().equals(now.getYear()))
                    .map(PayrollPeriodSummaryDTO::getId)
                    .findFirst()
                    .orElse(null);

            // Nếu không có kỳ cho tháng hiện tại thì lấy kỳ mới nhất
            if (effectivePeriodId == null && !periods.isEmpty()) {
                effectivePeriodId = periods.get(0).getId();
            }
        }

        model.addAttribute("rows",
                payrollManagerService.listPayrollRowsForManager(managerEmpId, q, status, effectivePeriodId));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("periodId", effectivePeriodId);
        model.addAttribute("periods", periods);
        model.addAttribute("statusOptions", List.of("", "DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED", "PAID"));

        final Integer selectedPeriodIdFinal = effectivePeriodId;

        String selectedPeriodLabel = periods.stream()
                .filter(p -> Objects.equals(p.getId(), selectedPeriodIdFinal))
                .findFirst()
                .map(p -> String.format("%02d/%d", p.getMonth(), p.getYear()))
                .orElse("Tất cả kỳ lương");

        model.addAttribute("selectedPeriodLabel", selectedPeriodLabel);

        return "manager/payroll-list";
    }

    @PostMapping("/payslips/bulk")
    public String bulk(@RequestParam("action") String action,
                       @RequestParam(value = "payslipIds", required = false) List<Integer> payslipIds,
                       @RequestParam(value = "reason", required = false) String reason,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "periodId", required = false) Integer periodId,
                       Principal principal,
                       Authentication authentication,
                       RedirectAttributes ra) {

        String redirectBack = "redirect:/manager/payroll/payslips";
        List<String> params = new ArrayList<>();

        if (q != null && !q.isBlank()) params.add("q=" + q);
        if (status != null && !status.isBlank()) params.add("status=" + status);
        if (periodId != null) params.add("periodId=" + periodId);

        if (!params.isEmpty()) {
            redirectBack += "?" + String.join("&", params);
        }

        if (payslipIds == null || payslipIds.isEmpty()) {
            ra.addFlashAttribute("msgType", "warning");
            ra.addFlashAttribute("msg", "Bạn chưa chọn dòng nào.");
            return redirectBack;
        }

        if ("reject".equalsIgnoreCase(action) && (reason == null || reason.trim().isEmpty())) {
            ra.addFlashAttribute("msgType", "warning");
            ra.addFlashAttribute("msg", "Bạn phải nhập lý do reject.");
            return redirectBack;
        }

        Integer reviewerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        Integer accessEmpId = reviewerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) accessEmpId = null;

        List<Integer> ids = payslipIds.stream().filter(Objects::nonNull).distinct().toList();
        int ok = 0, fail = 0;

        try {
            if ("approve".equalsIgnoreCase(action)) {
                for (Integer payslipId : ids) {
                    try {
                        payrollManagerService.approvePayslip(accessEmpId, payslipId);
                        ok++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
            } else if ("reject".equalsIgnoreCase(action)) {
                for (Integer payslipId : ids) {
                    try {
                        payrollManagerService.rejectPayslip(accessEmpId, reviewerEmpId, payslipId, reason);
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
        String actionLabel = "approve".equalsIgnoreCase(action) ? "Approve" : "Reject";

        ra.addFlashAttribute("msgType", msgType);
        ra.addFlashAttribute("msg", actionLabel + " xong: ok=" + ok + ", fail=" + fail);
        return redirectBack;
    }

    @PostMapping(value = "/payslips/bulk-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> bulkJson(@RequestParam("action") String action,
                                        @RequestParam(value = "payslipIds", required = false) List<Integer> payslipIds,
                                        @RequestParam(value = "reason", required = false) String reason,
                                        Principal principal,
                                        Authentication authentication) {

        Map<String, Object> res = new HashMap<>();

        if (payslipIds == null || payslipIds.isEmpty()) {
            res.put("ok", 0);
            res.put("fail", 0);
            res.put("msgType", "warning");
            res.put("msg", "Bạn chưa chọn dòng nào.");
            res.put("processedIds", List.of());
            return res;
        }

        Integer reviewerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        Integer accessEmpId = reviewerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) accessEmpId = null;

        List<Integer> ids = payslipIds.stream().filter(Objects::nonNull).distinct().toList();

        int ok = 0, fail = 0;
        List<Integer> processed = new ArrayList<>();

        if ("approve".equalsIgnoreCase(action)) {
            for (Integer payslipId : ids) {
                try {
                    payrollManagerService.approvePayslip(accessEmpId, payslipId);
                    ok++;
                    processed.add(payslipId);
                } catch (Exception e) {
                    fail++;
                }
            }

            res.put("ok", ok);
            res.put("fail", fail);
            res.put("processedIds", processed);
            res.put("msgType", fail > 0 ? "warning" : "success");
            res.put("msg", fail > 0 ? "Đã duyệt thành công " + ok + " phiếu lương. Thất bại: " + fail : "Đã duyệt thành công " + ok + " phiếu lương.");
            return res;
        }

        if ("reject".equalsIgnoreCase(action)) {
            if (reason == null || reason.trim().isEmpty()) {
                res.put("ok", 0);
                res.put("fail", ids.size());
                res.put("processedIds", List.of());
                res.put("msgType", "warning");
                res.put("msg", "Bạn phải nhập lý do reject.");
                return res;
            }

            for (Integer payslipId : ids) {
                try {
                    payrollManagerService.rejectPayslip(accessEmpId, reviewerEmpId, payslipId, reason);
                    ok++;
                    processed.add(payslipId);
                } catch (Exception e) {
                    fail++;
                }
            }

            res.put("ok", ok);
            res.put("fail", fail);
            res.put("processedIds", processed);
            res.put("msgType", fail > 0 ? "warning" : "success");
            res.put("msg", fail > 0 ? "Đã từ chối thành công " + ok + " phiếu lương. Thất bại: " + fail : "Đã từ chối thành công " + ok + " phiếu lương.");
            return res;
        }

        res.put("ok", 0);
        res.put("fail", ids.size());
        res.put("processedIds", List.of());
        res.put("msgType", "danger");
        res.put("msg", "Action không hợp lệ.");
        return res;
    }

    @PostMapping(value = "/payslips/{payslipId}/salary", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updatePayslipSalary(@PathVariable Integer payslipId,
                                                   @RequestParam("baseSalary") BigDecimal baseSalary,
                                                   Principal principal,
                                                   Authentication authentication) {

        Integer managerEmpId = currentEmployeeService.requireEmployee(principal).getId();

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR"));
        if (isAdminOrHr) managerEmpId = null;

        PayrollManagerService.SalaryUpdateResult r =
                payrollManagerService.updatePayslipBaseSalary(managerEmpId, payslipId, baseSalary);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", 1);
        res.put("payslipId", payslipId);
        res.put("baseSalary", r.baseSalary());
        res.put("netSalary", r.netSalary());
        res.put("slipStatus", r.slipStatus());
        return res;
    }

    @GetMapping(value = "/batches/{batchId}/employee-search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EmployeeSearchResultDTO> searchEmployeesForBatch(@PathVariable Integer batchId,
                                                                 @RequestParam(value = "q", required = false) String q,
                                                                 Principal principal,
                                                                 Authentication authentication) {

        Integer managerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHr) {
            managerEmpId = null;
        } else {
            managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        }

        return payrollManagerService.searchEmployeesForBatch(managerEmpId, batchId, q);
    }

    @PostMapping(value = "/batches/{batchId}/payslips/add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> addEmployeeToPayroll(@PathVariable Integer batchId,
                                                    @RequestParam("empId") Integer empId,
                                                    @RequestParam("baseSalary") BigDecimal baseSalary,
                                                    Principal principal,
                                                    Authentication authentication) {

        Integer managerEmpId;

        boolean isAdminOrHr = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR"));

        if (isAdminOrHr) {
            managerEmpId = null;
        } else {
            managerEmpId = currentEmployeeService.requireEmployee(principal).getId();
        }

        Integer payslipId = payrollManagerService.addEmployeeToPayroll(managerEmpId, batchId, empId, baseSalary);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", 1);
        res.put("batchId", batchId);
        res.put("empId", empId);
        res.put("payslipId", payslipId);
        res.put("msg", "Đã thêm nhân viên vào payroll.");
        return res;
    }
    @GetMapping(value = "/draft-batches", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<PayrollBatchSummaryDTO> getDraftBatches() {
        return payrollManagerService.listDraftBatches();
    }

    @PostMapping("/periods/{periodId}/generate")
    public String generateDraft(@PathVariable Integer periodId,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            Integer createdByEmpId = currentEmployeeService.requireEmployee(principal).getId();
            Integer batchId = payrollManagerService.generatePayrollDraft(periodId, createdByEmpId);
            ra.addFlashAttribute("msgType", "success");
            return "redirect:/manager/payroll/batches/" + batchId;
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/manager/payroll/periods";
        }
    }
}