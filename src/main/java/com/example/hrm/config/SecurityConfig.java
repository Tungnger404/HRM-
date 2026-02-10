package com.example.hrm.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    // =========================
    // PASSWORD ENCODER
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // LOGIN SUCCESS HANDLER
    // =========================
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    org.springframework.security.core.Authentication authentication
            ) throws IOException, ServletException {

                String ctx = request.getContextPath();

                boolean isAdmin = authentication.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isHr = authentication.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
                boolean isManager = authentication.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
                boolean isEmployee = authentication.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

                // 👉 Redirect theo role
                if (isAdmin) {
                    response.sendRedirect(ctx + "/dashboard/admin");
                } else if (isHr) {
                    response.sendRedirect(ctx + "/dashboard/hr");
                } else if (isManager) {
                    response.sendRedirect(ctx + "/dashboard/manager");
                } else if (isEmployee) {
                    response.sendRedirect(ctx + "/dashboard/employee");
                } else {
                    response.sendRedirect(ctx + "/login");
                }
            }
        };
    }

    // =========================
    // SECURITY FILTER CHAIN
    // =========================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // allow forward & error
                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                        ).permitAll()

                        // PUBLIC
                        .requestMatchers(
                                "/login", "/logout",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/assets/**",
                                "/webjars/**",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/api/**",
                                "/evaluation/**", "/training/**"
                        ).permitAll()

                        // DASHBOARD ROUTING
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers("/dashboard/hr").hasRole("HR")
                        .requestMatchers("/dashboard/manager").hasRole("MANAGER")
                        .requestMatchers("/dashboard/employee").hasRole("EMPLOYEE")

                        // MODULE PERMISSION
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "HR", "ADMIN")

                        // DEFAULT
                        .anyRequest().authenticated()
                )

                // LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}
