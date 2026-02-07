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

    // ================= LOGIN =================
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login/login"; // Thymeleaf: templates/login/login.html
    }

    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
            BindingResult br,
            HttpSession session,
            Model model
    ) {
        if (br.hasErrors()) return "login/login";

        try {
            authService.login(loginRequest, session);

            // TODO: sau này redirect theo role:
            // return "redirect:/hr/dashboard" ... tuỳ ROLE_NAME trong session
            return "redirect:/employees";

        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/login";
        }
    }

    // ================= REGISTER =================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "login/register"; // Thymeleaf: templates/login/register.html
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
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

    // ================= LOGOUT ======dfddff===========
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "redirect:/login?logout=1";
    }

    // ================= CHANGE PASSWORD (UI) =================
//hjjh
    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "login_register/change_password";
    }
}
