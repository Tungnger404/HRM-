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
    @Transactional(readOnly = true) // ✅ giữ session để đọc role
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (u.getActive() == null || !u.getActive()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
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
