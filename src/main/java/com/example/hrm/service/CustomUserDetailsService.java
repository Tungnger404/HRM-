package com.example.hrm.service;

import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        UserAccount u = userRepo.findByEmailIgnoreCase(usernameOrEmail).orElse(null);

        if (u == null) {
            u = userRepo.findByUsernameIgnoreCase(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        }

        if (u.getActive() == null || !u.getActive()) {
            throw new UsernameNotFoundException("User is disabled: " + usernameOrEmail);
        }

        if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("This account uses Google login only.");
        }

        String roleName = (u.getRole() == null || u.getRole().getRoleName() == null)
                ? "EMPLOYEE"
                : u.getRole().getRoleName().trim().toUpperCase();

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }
}