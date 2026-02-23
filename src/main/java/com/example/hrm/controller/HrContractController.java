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
                       Model model,
                       @ModelAttribute("msg") String msg,
                       @ModelAttribute("err") String err) {
        List<Contract> contracts = contractService.list(empId);
        model.addAttribute("contracts", contracts);
        model.addAttribute("empId", empId);
        return "hr/contracts_list";
    }

    @PostMapping("/create")
    public String create(@RequestParam("empId") Integer empId,
                         @RequestParam("startDate") LocalDate startDate,
                         @RequestParam(value = "endDate", required = false) LocalDate endDate,
                         @RequestParam("baseSalary") BigDecimal baseSalary,
                         @RequestParam(value = "status", required = false) String status,
                         RedirectAttributes ra) {
        try {
            contractService.create(empId, startDate, endDate, baseSalary, status);
            ra.addFlashAttribute("msg", "Created contract!");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/contracts?empId=" + empId;
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id,
                         @RequestParam("startDate") LocalDate startDate,
                         @RequestParam(value = "endDate", required = false) LocalDate endDate,
                         @RequestParam("baseSalary") BigDecimal baseSalary,
                         @RequestParam("status") String status,
                         RedirectAttributes ra) {
        try {
            Contract c = contractService.update(id, startDate, endDate, baseSalary, status);
            ra.addFlashAttribute("msg", "Updated contract!");
            return "redirect:/hr/contracts?empId=" + c.getEmployee().getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/contracts";
        }
    }

    @PostMapping("/{id}/terminate")
    public String terminate(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Contract c = contractService.get(id);
            Integer empId = c.getEmployee().getEmpId();
            contractService.terminate(id);
            ra.addFlashAttribute("msg", "Terminated contract!");
            return "redirect:/hr/contracts?empId=" + empId;
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/contracts";
        }
    }
}
