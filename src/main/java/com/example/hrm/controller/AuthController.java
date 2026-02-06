package com.example.hrm.controller;

import com.example.hrm.dto.LoginRequest;
import com.example.hrm.dto.RegisterRequest;
import com.example.hrm.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login/login";
    }

    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute LoginRequest loginRequest,
            BindingResult br,
            HttpSession session,
            Model model
    ) {
        if (br.hasErrors()) return "login/login";

        try {
            authService.login(loginRequest, session);
            return "redirect:/employees";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "login/register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) return "login/register";

        try {
            authService.register(registerRequest);
            return "redirect:/login?registered=1";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=1";
    }
}
