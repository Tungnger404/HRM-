package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeSearchResultDTO;
import com.example.hrm.dto.PayrollBatchSummaryDTO;
import com.example.hrm.service.CurrentEmployeeService;
import com.example.hrm.service.PayrollExportService;
import com.example.hrm.service.PayrollManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hr/payroll")
public class HrPayrollManageController {

    private final PayrollManagerService payrollManagerService;
    private final PayrollExportService payrollExportService;
    private final CurrentEmployeeService currentEmployeeService;

    @GetMapping("/periods")
    public String periods(Model model) {
        model.addAttribute("periods", payrollManagerService.listPayrollPeriods());
        return "hr/payroll-period-list";
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
        return "redirect:/hr/payroll/periods";
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
        return "redirect:/hr/payroll/periods";
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
        return "hr/payroll-batch-list";
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
        return "redirect:/hr/payroll/periods";
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
        return "redirect:/hr/payroll/periods";
    }

    @PostMapping("/periods/{periodId}/generate")
    public String generateDraft(@PathVariable Integer periodId,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            Integer createdByEmpId = currentEmployeeService.requireEmployee(principal).getId();
            Integer batchId = payrollManagerService.generatePayrollDraft(periodId, createdByEmpId);
            ra.addFlashAttribute("msgType", "success");
            return "redirect:/hr/payroll/batches/" + batchId;
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/hr/payroll/periods";
        }
    }

    @GetMapping("/batches/{batchId}")
    public String batchDetail(@PathVariable Integer batchId, Model model) {
        model.addAttribute("b", payrollManagerService.viewBatchDetail(batchId));
        return "hr/payroll-detail";
    }

    @PostMapping("/batches/{batchId}/delete")
    public String deleteDraftBatch(@PathVariable Integer batchId, RedirectAttributes ra) {
        try {
            Integer periodId = payrollManagerService.deleteDraftBatch(batchId);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã xóa batch DRAFT thành công.");
            return "redirect:/hr/payroll/periods/" + periodId + "/batches";
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/hr/payroll/batches/" + batchId;
        }
    }

    @PostMapping("/batches/{batchId}/submit")
    public String submitForApproval(@PathVariable Integer batchId) {
        payrollManagerService.submitBatchForApproval(batchId);
        return "redirect:/hr/payroll/batches/" + batchId;
    }

    @PostMapping("/batches/{batchId}/approve")
    public String approve(@PathVariable Integer batchId, Principal principal) {
        Integer approverEmpId = currentEmployeeService.requireEmployee(principal).getId();
        payrollManagerService.approveBatch(batchId, approverEmpId);
        return "redirect:/hr/payroll/batches/" + batchId;
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
        return "redirect:/hr/payroll/batches/" + batchId;
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

    @GetMapping(value = "/batches/{batchId}/employee-search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EmployeeSearchResultDTO> searchEmployeesForBatch(@PathVariable Integer batchId,
                                                                 @RequestParam(value = "q", required = false) String q) {
        return payrollManagerService.searchEmployeesForBatch(null, batchId, q);
    }

    @PostMapping(value = "/batches/{batchId}/payslips/add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> addEmployeeToPayroll(@PathVariable Integer batchId,
                                                    @RequestParam("empId") Integer empId,
                                                    @RequestParam("baseSalary") BigDecimal baseSalary) {
        Integer payslipId = payrollManagerService.addEmployeeToPayroll(null, batchId, empId, baseSalary);

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
}