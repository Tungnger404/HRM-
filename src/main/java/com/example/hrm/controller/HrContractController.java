package com.example.hrm.controller;

import com.example.hrm.entity.Contract;
import com.example.hrm.entity.Employee;
import com.example.hrm.service.ContractService;
import com.example.hrm.service.EmployeeService;
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
    private final EmployeeService employeeService;

    public HrContractController(ContractService contractService, EmployeeService employeeService) {
        this.contractService = contractService;
        this.employeeService = employeeService;
    }

    // Màn 1: bấm sidebar Contract -> list employee
    @GetMapping
    public String employees(@RequestParam(value = "q", required = false) String q,
                            @RequestParam(value = "status", required = false) String status,
                            Model model,
                            @ModelAttribute("msg") String msg,
                            @ModelAttribute("err") String err) {

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("employees", employeeService.list(q, status));

        return "hr/contracts_employees";
    }

    // Màn 2: list contract của 1 employee
    @GetMapping("/{empId}/list")
    public String listByEmployee(@PathVariable Integer empId,
                                 Model model,
                                 @ModelAttribute("msg") String msg,
                                 @ModelAttribute("err") String err) {

        Employee employee = employeeService.getById(empId);
        List<Contract> contracts = contractService.listByEmployee(empId);

        model.addAttribute("employee", employee);
        model.addAttribute("contracts", contracts);
        model.addAttribute("empId", empId);

        return "hr/contracts_list";
    }

    // Tạo mới contract cho employee
    @PostMapping("/{empId}/create")
    public String create(@PathVariable Integer empId,
                         @RequestParam(value = "contractNumber", required = false) String contractNumber,
                         @RequestParam("contractType") String contractType,
                         @RequestParam("startDate") LocalDate startDate,
                         @RequestParam(value = "endDate", required = false) LocalDate endDate,
                         @RequestParam("baseSalary") BigDecimal baseSalary,
                         @RequestParam(value = "status", required = false) String status,
                         RedirectAttributes ra) {
        try {
            contractService.create(empId, contractNumber, contractType, startDate, endDate, baseSalary, status);
            ra.addFlashAttribute("msg", "Created new contract successfully. Old ACTIVE contracts were moved to TERMINATED.");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/contracts/" + empId + "/list";
    }

    // Màn detail để edit 1 contract
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id,
                         Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("err") String err) {
        Contract contract = contractService.get(id);
        model.addAttribute("contract", contract);
        return "hr/contract_detail";
    }

    @PostMapping("/detail/{id}/detail-update")
    public String updateDetail(@PathVariable Integer id,
                               @RequestParam("startDate") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) LocalDate endDate,
                               @RequestParam("baseSalary") BigDecimal baseSalary,
                               @RequestParam("status") String status,
                               @RequestParam("contractType") String contractType,
                               @RequestParam(value = "contractNumber", required = false) String contractNumber,
                               RedirectAttributes ra) {
        try {
            Contract updated = contractService.updateDetail(
                    id, startDate, endDate, baseSalary, status, contractType, contractNumber
            );
            ra.addFlashAttribute("msg", "Contract updated successfully.");
            return "redirect:/hr/contracts/detail/" + updated.getId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/contracts/detail/" + id;
        }
    }

    @PostMapping("/detail/{id}/terminate")
    public String terminate(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Contract c = contractService.get(id);
            Integer empId = c.getEmployee().getEmpId();

            contractService.terminate(id);

            ra.addFlashAttribute("msg", "Contract terminated successfully.");
            return "redirect:/hr/contracts/" + empId + "/list";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/contracts";
        }
    }
}