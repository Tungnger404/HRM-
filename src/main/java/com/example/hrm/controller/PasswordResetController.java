package com.example.hrm.controller;

import com.example.hrm.dto.ForgotPasswordRequest;
import com.example.hrm.dto.ResetPasswordRequest;
import com.example.hrm.dto.VerifyOtpRequest;
import com.example.hrm.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/forgot-password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "login/forgot-password";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                          BindingResult br,
                          Model model) {
        if (br.hasErrors()) {
            return "login/forgot-password";
        }

        try {
            passwordResetService.sendOtp(request.getEmail());
            return "redirect:/forgot-password/verify?email=" + request.getEmail();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/forgot-password";
        }
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam String email, Model model) {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("verifyOtpRequest", request);
        return "login/verify-otp";
    }

    @PostMapping("/verify")
    public String verifyOtp(@Valid @ModelAttribute("verifyOtpRequest") VerifyOtpRequest request,
                            BindingResult br,
                            Model model) {
        if (br.hasErrors()) {
            return "login/verify-otp";
        }

        try {
            passwordResetService.verifyOtp(request.getEmail(), request.getOtp());
            return "redirect:/forgot-password/reset?email=" + request.getEmail();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/verify-otp";
        }
    }

    @GetMapping("/reset")
    public String resetPage(@RequestParam String email, Model model) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        model.addAttribute("resetPasswordRequest", request);
        return "login/reset-password";
    }

    @PostMapping("/reset")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                                BindingResult br,
                                Model model) {
        if (br.hasErrors()) {
            return "login/reset-password";
        }

        try {
            passwordResetService.resetPassword(
                    request.getEmail(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            return "redirect:/login?resetSuccess=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login/reset-password";
        }
    }
}