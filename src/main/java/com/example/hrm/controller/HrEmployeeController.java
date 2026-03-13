package com.example.hrm.controller;

import com.example.hrm.dto.EmployeeAdd;
import com.example.hrm.dto.EmployeeAssignmentUpdateRequest;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeDepartmentTransferHistoryRepository;
import com.example.hrm.repository.EmployeeJobChangeHistoryRepository;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.service.EmployeeService;
import com.example.hrm.service.HrEmployeeAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/employees")
public class HrEmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final HrEmployeeAssignmentService hrEmployeeAssignmentService;
    private final EmployeeDepartmentTransferHistoryRepository transferHistoryRepository;
    private final EmployeeJobChangeHistoryRepository jobChangeHistoryRepository;

    public HrEmployeeController(EmployeeService employeeService,
                                DepartmentRepository departmentRepository,
                                JobPositionRepository jobPositionRepository,
                                HrEmployeeAssignmentService hrEmployeeAssignmentService,
                                EmployeeDepartmentTransferHistoryRepository transferHistoryRepository,
                                EmployeeJobChangeHistoryRepository jobChangeHistoryRepository) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.hrEmployeeAssignmentService = hrEmployeeAssignmentService;
        this.transferHistoryRepository = transferHistoryRepository;
        this.jobChangeHistoryRepository = jobChangeHistoryRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       Model model) {
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("employees", employeeService.list(q, status));
        return "hr/employee_list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Employee e = employeeService.getById(id);
        model.addAttribute("e", e);
        return "hr/employee_detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute EmployeeAdd form, RedirectAttributes ra) {
        try {
            employeeService.update(form);
            ra.addFlashAttribute("msg", "Saved changes!");
            return "redirect:/hr/employees/" + form.getEmpId();
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/employees/" + form.getEmpId();
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            employeeService.delete(id);
            ra.addFlashAttribute("msg", "Deleted employee!");
            return "redirect:/hr/employees";
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
            return "redirect:/hr/employees/" + id;
        }
    }

    @GetMapping("/{empId}/assignment")
    public String showAssignmentForm(@PathVariable Integer empId, Model model) {
        Employee employee = employeeService.getById(empId);

        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobPositionRepository.findByActiveTrueOrderByTitleAsc());
        model.addAttribute("form", new EmployeeAssignmentUpdateRequest());
        model.addAttribute("deptHistories", transferHistoryRepository.findByEmpIdOrderByTransferDateDesc(empId));
        model.addAttribute("jobHistories", jobChangeHistoryRepository.findByEmpIdOrderByChangeDateDesc(empId));

        return "hr/employee-assignment";
    }

    @PostMapping("/{empId}/assignment")
    public String updateAssignment(@PathVariable Integer empId,
                                   @ModelAttribute("form") EmployeeAssignmentUpdateRequest form,
                                   RedirectAttributes ra) {
        try {
            hrEmployeeAssignmentService.updateAssignment(empId, form);
            ra.addFlashAttribute("msg", "Employee assignment updated successfully");
        } catch (Exception ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/hr/employees/" + empId + "/assignment";
    }
}