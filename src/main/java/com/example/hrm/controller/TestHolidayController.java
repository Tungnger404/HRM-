package com.example.hrm.controller;

import com.example.hrm.entity.Holiday;
import com.example.hrm.repository.HolidayRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestHolidayController {
    
    private final HolidayRepository holidayRepository;
    
    public TestHolidayController(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }
    
    @GetMapping("/test-holidays")
    public List<Holiday> getHolidays() {
        return holidayRepository.findAll();
    }
}
