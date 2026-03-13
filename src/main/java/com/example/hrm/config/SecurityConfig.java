package com.example.hrm.config;

import com.example.hrm.service.CustomOAuth2UserService;
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

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/careers",
                                "/login",
                                "/logout",
                                "/register", "/register/**",
                                "/forgot-password", "/forgot-password/**",
                                "/oauth2/**", "/login/oauth2/**",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/assets/**",
                                "/webjars/**",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers("/dashboard/hr").hasRole("HR")
                        .requestMatchers("/dashboard/manager").hasRole("MANAGER")
                        .requestMatchers("/dashboard/employee").hasAnyRole("EMPLOYEE", "HR", "MANAGER", "ADMIN")

                        .requestMatchers("/hr/**").hasAnyRole("HR", "ADMIN")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/employee/**").authenticated()

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            boolean isHr = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
                            boolean isManager = authentication.getAuthorities()
                                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

                            if (isAdmin) {
                                response.sendRedirect("/dashboard/admin");
                            } else if (isHr) {
                                response.sendRedirect("/dashboard/hr");
                            } else if (isManager) {
                                response.sendRedirect("/dashboard/manager");
                            } else {
                                response.sendRedirect("/dashboard/employee");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )

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