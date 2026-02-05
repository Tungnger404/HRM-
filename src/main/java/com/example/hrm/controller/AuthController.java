package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        // trỏ đúng tới: /WEB-INF/views/login_register/login.jsp
        return "login_register/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "login_register/register";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "login_register/change_password";
    }
}
