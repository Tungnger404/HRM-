package com.example.hrm.controller;

import com.example.hrm.dto.PayrollPeriodSummaryDTO;
import com.example.hrm.dto.PayrollRowDTO;
import com.example.hrm.service.BenefitService;
import com.example.hrm.service.HrPayrollService;
import com.example.hrm.service.PayrollManagerService;
import com.example.hrm.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hr/payroll")
public class HrPayrollController {

    private final HrPayrollService hrPayrollService;
    private final BenefitService benefitService;
    private final PayrollManagerService payrollManagerService;
    private final PayslipRepository payslipRepo;

    // ─────────────────────────────────────────────────────────────
    // Full payslip list for HR (tất cả payslip, không lọc manager)
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/payslips")
    public String payrollList(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "periodId", required = false) Integer periodId,
                              Model model) {

        List<PayrollPeriodSummaryDTO> periods = hrPayrollService.listPayrollPeriods();

        Integer effectivePeriodId = periodId;

        // Tự chọn kỳ có dữ liệu gần nhất khi mới vào trang
        // Tự chọn kỳ tháng hiện tại khi mới vào trang
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

        model.addAttribute("rows", hrPayrollService.listAllPayrollRows(q, status, effectivePeriodId));
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

        return "hr/payroll-list";
    }

    @PostMapping(value = "/payslips/bulk-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> bulkJson(@RequestParam("action") String action,
                                        @RequestParam(value = "payslipIds", required = false) List<Integer> payslipIds,
                                        @RequestParam(value = "reason", required = false) String reason) {

        Map<String, Object> res = new HashMap<>();

        if (payslipIds == null || payslipIds.isEmpty()) {
            res.put("ok", 0);
            res.put("fail", 0);
            res.put("msgType", "warning");
            res.put("msg", "Bạn chưa chọn dòng nào.");
            res.put("processedIds", List.of());
            return res;
        }

        List<Integer> ids = payslipIds.stream().filter(Objects::nonNull).distinct().toList();
        int ok = 0, fail = 0;
        List<Integer> processed = new ArrayList<>();

        if ("approve".equalsIgnoreCase(action)) {
            for (Integer payslipId : ids) {
                try {
                    // HR duyệt: managerEmpId = null (bỏ qua kiểm tra manager)
                    payrollManagerService.approvePayslip(null, payslipId);
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
                    payrollManagerService.rejectPayslip(null, null, payslipId, reason);
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

    // ─────────────────────────────────────────────────────────────
    // Rejected payslips section
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/rejected")
    public String rejectedList(@RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "periodId", required = false) Integer periodId,
                               Model model) {

        List<PayrollPeriodSummaryDTO> periods = hrPayrollService.listPayrollPeriods();

        model.addAttribute("rows", hrPayrollService.listRejectedPayrollRowsForHr(q, periodId));
        model.addAttribute("q", q);
        model.addAttribute("periodId", periodId);
        model.addAttribute("periods", periods);

        String selectedPeriodLabel = periods.stream()
                .filter(p -> Objects.equals(p.getId(), periodId))
                .findFirst()
                .map(p -> String.format("%02d/%d", p.getMonth(), p.getYear()))
                .orElse("Tất cả kỳ lương");

        model.addAttribute("selectedPeriodLabel", selectedPeriodLabel);

        return "hr/payroll-rejected-list";
    }

    @GetMapping("/rejected/{payslipId}")
    public String rejectedDetail(@PathVariable Integer payslipId, Model model,
                                 RedirectAttributes ra) {
        try {
            model.addAttribute("r", hrPayrollService.getRejectedPayslipForHr(payslipId));
            model.addAttribute("activeBenefits", benefitService.listActive());
            return "hr/payroll-rejected-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "warning");
            ra.addFlashAttribute("msg", "Payslip này không ở trạng thái REJECTED: " + e.getMessage());
            return "redirect:/hr/payroll/rejected";
        }
    }

    @PostMapping("/rejected/{payslipId}/salary")
    public String updateRejectedSalary(@PathVariable Integer payslipId,
                                       @RequestParam("baseSalary") BigDecimal baseSalary,
                                       RedirectAttributes ra) {
        try {
            hrPayrollService.updateRejectedPayslipBaseSalary(payslipId, baseSalary);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã cập nhật base salary cho payslip bị reject.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/hr/payroll/rejected/" + payslipId;
    }

    @PostMapping("/rejected/{payslipId}/benefits/add")
    public String addBenefitToRejected(@PathVariable Integer payslipId,
                                       @RequestParam("benefitId") Integer benefitId,
                                       RedirectAttributes ra) {
        try {
            hrPayrollService.addActiveBenefitToRejectedPayslip(payslipId, benefitId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã thêm khoản lương vào payslip bị reject.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/hr/payroll/rejected/" + payslipId;
    }

    @GetMapping("/payslips/{payslipId}")
    public String payslipDetail(@PathVariable Integer payslipId, Model model) {
        model.addAttribute("p", hrPayrollService.getPayslipDetailForHr(payslipId));
        return "hr/payslip-detail";
    }

    @PostMapping("/payslips/{id}/delete")
    public String deletePayslip(@PathVariable("id") Integer payslipId, RedirectAttributes ra) {
        try {
            Integer periodId = null;
            com.example.hrm.entity.Payslip p = payslipRepo.findById(payslipId).orElse(null);
            if (p != null && p.getBatch() != null && p.getBatch().getPeriod() != null) {
                periodId = p.getBatch().getPeriod().getId();
            }

            // HR: empId = null
            payrollManagerService.deletePayslipFromBatch(null, payslipId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã xóa phiếu lương khỏi bảng lương.");

            if (periodId != null) {
                return "redirect:/hr/payroll/payslips?periodId=" + periodId;
            }
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/hr/payroll/payslips";
    }

    @PostMapping("/rejected/{payslipId}/reopen")
    public String reopen(@PathVariable Integer payslipId, RedirectAttributes ra) {
        try {
            hrPayrollService.reopenPayslipByHr(payslipId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "HR đã reopen payslip để manager review lại.");
            return "redirect:/hr/payroll/rejected";
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/hr/payroll/rejected/" + payslipId;
        }
    }
}