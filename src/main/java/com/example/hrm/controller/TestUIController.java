package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestUIController {

    @GetMapping("/test-ui")
    public String testUI() {
        return "login/login"; // đường dẫn tới templates/layout/dashboard.html
    }
}

