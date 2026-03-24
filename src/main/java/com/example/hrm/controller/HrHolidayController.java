package com.example.hrm.controller;

import com.example.hrm.entity.Holiday;
import com.example.hrm.repository.HolidayRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/hr/holidays")
public class HrHolidayController {

    private final HolidayRepository holidayRepository;

    public HrHolidayController(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @GetMapping
    public String listHolidays(Model model) {
        model.addAttribute("holidays", holidayRepository.findAllByOrderByHolidayDateDesc());
        return "hr/holidays";
    }

    @PostMapping("/save")
    public String saveHoliday(@RequestParam String title,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false, defaultValue = "INACTIVE") String status,
                              RedirectAttributes ra) {
        try {
            Holiday holiday = new Holiday(title, date, description, status);
            holidayRepository.save(holiday);
            ra.addFlashAttribute("success", "Holiday saved successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error saving holiday: " + e.getMessage());
        }
        return "redirect:/hr/holidays";
    }

    @PostMapping("/edit/{id}")
    public String editHoliday(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false, defaultValue = "INACTIVE") String status,
                              RedirectAttributes ra) {
        try {
            Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid holiday ID: " + id));
            holiday.setTitle(title);
            holiday.setHolidayDate(date);
            holiday.setDescription(description);
            holiday.setStatus(status);
            holidayRepository.save(holiday);
            ra.addFlashAttribute("success", "Holiday updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error updating holiday: " + e.getMessage());
        }
        return "redirect:/hr/holidays";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid holiday Id:" + id));
            if ("ACTIVE".equals(holiday.getStatus())) {
                holiday.setStatus("INACTIVE");
            } else {
                holiday.setStatus("ACTIVE");
            }
            holidayRepository.save(holiday);
            ra.addFlashAttribute("success", "Holiday status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error updating status: " + e.getMessage());
        }
        return "redirect:/hr/holidays";
    }

    @PostMapping("/delete/{id}")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes ra) {
        try {
            holidayRepository.deleteById(id);
            ra.addFlashAttribute("success", "Holiday deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Error deleting holiday: " + e.getMessage());
        }
        return "redirect:/hr/holidays";
    }
}
