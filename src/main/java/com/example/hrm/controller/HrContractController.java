package com.example.hrm.controller;

import com.example.hrm.entity.Contract;
import com.example.hrm.service.ContractService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/hr/contracts")
public class HrContractController {

    private final ContractService contractService;

    public HrContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public String list(@RequestParam(value = "empId", required = false) Integer empId,
                       @RequestParam(value = "contractType", required = false) String contractType,
                       @RequestParam(value = "expiringOnly", required = false, defaultValue = "false") boolean expiringOnly,
                       Model model,
                       @ModelAttribute("msg") String msg,
                       @ModelAttribute("err") String err) {

        List<Contract> contracts;

        if (expiringOnly) {
            contracts = contractService.listExpiringWithin30Days();
        } else {
            contracts = contractService.list(empId);
        }

        if (contractType != null && !contractType.isBlank()) {
            contracts = contracts.stream()
                    .filter(c -> c.getContractType() != null
                            && contractType.equalsIgnoreCase(c.getContractType()))
                    .toList();
        }

        model.addAttribute("contracts", contracts);
        model.addAttribute("empId", empId);
        model.addAttribute("contractType", contractType);
        model.addAttribute("expiringOnly", expiringOnly);

        return "hr/contracts_list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id,
                         Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("err") String err) {
        Contract contract = contractService.get(id);
        model.addAttribute("contract", contract);
        return "hr/contract_detail";
    }

    @PostMapping("/{id}/detail-update")
    public String updateDetail(@PathVariable Integer id,
                               @RequestParam("startDate") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) LocalDate endDate,
                               @RequestParam("baseSalary") BigDecimal baseSalary,
                               @RequestParam("status") String status,
                               @RequestParam("contractType") String contractType,
                               RedirectAttributes ra) {
        try {
            contractService.updateDetail(id, startDate, endDate, baseSalary, status, contractType);
            ra.addFlashAttribute("msg", "Contract updated successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/contracts/" + id;
    }

    @PostMapping("/{id}/terminate")
    public String terminate(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Contract c = contractService.get(id);
            Integer empId = c.getEmployee().getEmpId();

            contractService.terminate(id);

            ra.addFlashAttribute("msg", "Contract terminated successfully.");
            return "redirect:/hr/contracts?empId=" + empId;
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/contracts";
        }
    }

    @PostMapping("/{id}/approve-official")
    public String approveOfficial(@PathVariable Integer id,
                                  RedirectAttributes ra) {
        try {
            contractService.approveOfficial(id);
            ra.addFlashAttribute("msg", "Employee has been approved as active official staff.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/contracts?expiringOnly=true";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id,
                         RedirectAttributes ra) {
        try {
            contractService.rejectEmployee(id);
            ra.addFlashAttribute("msg", "Employee has been rejected.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/contracts?expiringOnly=true";
    }
}