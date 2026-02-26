package com.example.hrm.controller.hr;

import com.example.hrm.dto.JobPositionForm;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.view.JobEmployeeCountView;
import com.example.hrm.service.JobPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hr/job-positions")
public class HrJobPositionController {

    private final JobPositionService service;
    private final EmployeeRepository employeeRepo;   // ✅ thêm

    // ==========================
    // LIST + EMPLOYEE COUNT
    // ==========================
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<JobPosition> data = service.search(q, level, active, page, size);

        // ✅ Lấy jobIds trong page hiện tại
        List<Integer> jobIds = data.getContent().stream()
                .map(JobPosition::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Integer, Long> empCountMap = new HashMap<>();

        if (!jobIds.isEmpty()) {
            List<JobEmployeeCountView> counts = employeeRepo.countByJobIds(jobIds);
            for (JobEmployeeCountView c : counts) {
                empCountMap.put(c.getJobId(), c.getCnt());
            }
        }

        model.addAttribute("data", data);
        model.addAttribute("empCountMap", empCountMap);  // ✅ gửi sang view
        model.addAttribute("q", q);
        model.addAttribute("level", level);
        model.addAttribute("active", active);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        return "hr/job_positions/list";
    }

    // ==========================
    // VIEW EMPLOYEES IN JOB
    // ==========================
    @GetMapping("/{id}/employees")
    public String employeesByJob(@PathVariable Integer id, Model model) {

        JobPosition jp = service.getById(id);
        List<Employee> employees = employeeRepo.findByJobId(id);

        model.addAttribute("jp", jp);
        model.addAttribute("employees", employees);

        return "hr/job_positions/employees";
    }

    // ==========================
    // CREATE
    // ==========================
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("f", new JobPositionForm());
        model.addAttribute("mode", "create");
        return "hr/job_positions/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("f") JobPositionForm f,
            BindingResult br,
            RedirectAttributes ra,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "hr/job_positions/form";
        }

        try {
            Integer id = service.create(f);
            ra.addFlashAttribute("msg", "Created job position ID = " + id);
            return "redirect:/hr/job-positions";
        } catch (RuntimeException ex) {
            model.addAttribute("mode", "create");
            model.addAttribute("error", ex.getMessage());
            return "hr/job_positions/form";
        }
    }

    // ==========================
    // EDIT
    // ==========================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("f", service.getFormById(id));
        model.addAttribute("mode", "edit");
        return "hr/job_positions/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Integer id,
            @Valid @ModelAttribute("f") JobPositionForm f,
            BindingResult br,
            RedirectAttributes ra,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "hr/job_positions/form";
        }

        try {
            service.update(id, f);
            ra.addFlashAttribute("msg", "Updated job position ID = " + id);
            return "redirect:/hr/job-positions";
        } catch (RuntimeException ex) {
            model.addAttribute("mode", "edit");
            model.addAttribute("error", ex.getMessage());
            return "hr/job_positions/form";
        }
    }

    // ==========================
    // ACTIVATE / DEACTIVATE
    // ==========================
    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Integer id, RedirectAttributes ra) {
        service.setActive(id, false);
        ra.addFlashAttribute("msg", "Deactivated job position ID = " + id);
        return "redirect:/hr/job-positions";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Integer id, RedirectAttributes ra) {
        service.setActive(id, true);
        ra.addFlashAttribute("msg", "Activated job position ID = " + id);
        return "redirect:/hr/job-positions";
    }
}