package com.example.hrm.service;

import com.example.hrm.entity.Role;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.RoleRepository;
import com.example.hrm.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        Map<String, Object> attrs = oauth2User.getAttributes();

        String googleId = (String) attrs.get("sub");
        String email = (String) attrs.get("email");
        String name = (String) attrs.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account has no email");
        }

        UserAccount user = userRepo.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            user = userRepo.findByEmailIgnoreCase(email).orElse(null);
        }

        if (user == null) {
            Role defaultRole = roleRepo.findAll().stream()
                    .filter(r -> r.getRoleName() != null && r.getRoleName().equalsIgnoreCase("EMPLOYEE"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role EMPLOYEE not found"));

            String baseUsername = email.split("@")[0];
            String username = buildUniqueUsername(baseUsername);

            user = UserAccount.builder()
                    .username(username)
                    .email(email.toLowerCase())
                    .passwordHash(null)
                    .role(defaultRole)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .googleId(googleId)
                    .authProvider("GOOGLE")
                    .build();
        } else {
            user.setEmail(email.toLowerCase());
            user.setGoogleId(googleId);
            user.setAuthProvider("GOOGLE");
            if (user.getActive() == null) {
                user.setActive(true);
            }
        }

        userRepo.save(user);
        return oauth2User;
    }

    private String buildUniqueUsername(String base) {
        String username = base;
        int i = 1;
        while (userRepo.existsByUsernameIgnoreCase(username)) {
            username = base + i;
            i++;
        }
        return username;
    }
}