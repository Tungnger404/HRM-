package com.example.hrm.config;

import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.UserAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserAccountRepository userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        UserAccount user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found after Google login"));

        String roleName = (user.getRole() == null || user.getRole().getRoleName() == null)
                ? "EMPLOYEE"
                : user.getRole().getRoleName().trim().toUpperCase();

        switch (roleName) {
            case "ADMIN" -> response.sendRedirect("/dashboard/admin");
            case "HR" -> response.sendRedirect("/dashboard/hr");
            case "MANAGER" -> response.sendRedirect("/dashboard/manager");
            default -> response.sendRedirect("/dashboard/employee");
        }
    }
}