package com.example.hrm.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Forward & error
                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                        ).permitAll()

                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/",
                                "/login", "/logout",
                                "/register", "/register/**",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/assets/**",
                                "/webjars/**",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/evaluation/**", "/training/**", // For testing without login
                                "/hr/kpi/**", "/manager/evaluation/**", "/api/notifications/**",
                                "/offer/accept/**",
                                "/offer/reject/**"
                        ).permitAll()

                        // ===== DASHBOARD (DEMO: mở hết) =====
                        .requestMatchers("/dashboard").permitAll()
                        .requestMatchers("/dashboard/admin").permitAll()
                        .requestMatchers("/dashboard/hr").permitAll()
                        .requestMatchers("/dashboard/manager").permitAll()
                        .requestMatchers("/dashboard/employee").permitAll()

                        // ===== MODULE PERMISSION (DEMO: mở hết) =====
                        .requestMatchers("/hr/**").permitAll()
                        .requestMatchers("/manager/**").permitAll()
                        .requestMatchers("/employee/**").permitAll()
                        .requestMatchers("/bank/**").permitAll()

                        // ===== DEFAULT =====
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {

                            boolean isAdmin = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            boolean isHr = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
                            boolean isManager = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
                            boolean isEmployee = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

                            if (isAdmin) {
                                response.sendRedirect("/dashboard/admin");
                            } else if (isHr) {
                                response.sendRedirect("/dashboard/hr");
                            } else if (isManager) {
                                response.sendRedirect("/dashboard/manager");
                            } else if (isEmployee) {
                                response.sendRedirect("/dashboard/employee");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // ===== LOGOUT =====
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}