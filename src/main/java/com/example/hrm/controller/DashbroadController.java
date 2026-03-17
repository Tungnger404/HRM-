package com.example.hrm.controller;

import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.view.EmployeeStatusCountView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashbroadController {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final DepartmentRepository departmentRepository;

    public DashbroadController(EmployeeRepository employeeRepository,
                               ContractRepository contractRepository,
                               DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isHr = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isAdmin) return "redirect:/dashboard/admin";
        if (isHr) return "redirect:/dashboard/hr";
        if (isManager) return "redirect:/dashboard/manager";
        if (isEmployee) return "redirect:/dashboard/employee";

        return "redirect:/login";
    }

    @GetMapping("/dashboard/admin")
    public String adminDash(Model model) {
        model.addAttribute("sidebar", "sidebar-admin.html");
        return "auth/AdminDashbroad";
    }

    @GetMapping("/dashboard/hr")
    public String hrDash(Model model) {
        long totalEmployees = employeeRepository.count();
        long totalDepartments = departmentRepository.count();

        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);
        long expiringContracts = contractRepository.countByEndDateBetween(today, next30Days);

        List<EmployeeStatusCountView> rawStatusList = employeeRepository.countEmployeesGroupByStatus();

        Map<String, Long> rawStatusMap = rawStatusList.stream()
                .collect(Collectors.toMap(
                        EmployeeStatusCountView::getStatus,
                        EmployeeStatusCountView::getTotal
                ));

        List<String> preferredOrder = List.of(
                "PROBATION",
                "ACTIVE",
                "OFFICIAL",
                "INACTIVE",
                "RESIGNED",
                "TERMINATED",
                "UNKNOWN"
        );

        Map<String, Long> orderedStatusMap = new LinkedHashMap<>();

        for (String status : preferredOrder) {
            if (rawStatusMap.containsKey(status)) {
                orderedStatusMap.put(status, rawStatusMap.get(status));
            }
        }

        for (Map.Entry<String, Long> entry : rawStatusMap.entrySet()) {
            if (!orderedStatusMap.containsKey(entry.getKey())) {
                orderedStatusMap.put(entry.getKey(), entry.getValue());
            }
        }

        model.addAttribute("sidebar", "sidebar-hr.html");
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("expiringContracts", expiringContracts);
        model.addAttribute("statusStats", orderedStatusMap);

        return "auth/HrDashbroad";
    }

    @GetMapping("/dashboard/manager")
    public String managerDash(Model model) {
        model.addAttribute("sidebar", "sidebar-manager.html");
        return "auth/ManagerDashbroad";
    }

    @GetMapping("/dashboard/employee")
    public String employeeDash(Model model) {
        model.addAttribute("sidebar", "sidebar-employee.html");
        return "auth/EmployeeDashbroad";
    }
}