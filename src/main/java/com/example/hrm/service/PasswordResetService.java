package com.example.hrm.service;

import com.example.hrm.entity.PasswordResetOtp;
import com.example.hrm.entity.UserAccount;
import com.example.hrm.repository.PasswordResetOtpRepository;
import com.example.hrm.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserAccountRepository userRepo;
    private final PasswordResetOtpRepository otpRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        UserAccount user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        if (user.getActive() == null || !user.getActive()) {
            throw new RuntimeException("Account is disabled");
        }

        otpRepo.deleteByExpiresAtBefore(LocalDateTime.now());

        String otp = generateOtp();

        PasswordResetOtp entity = PasswordResetOtp.builder()
                .user(user)
                .email(user.getEmail())
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepo.saveAndFlush(entity);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        String normalizedEmail = email.trim().toLowerCase();

        UserAccount user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        PasswordResetOtp otp = otpRepo.findTopByUserAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(user, otpCode.trim())
                .orElseThrow(() -> new RuntimeException("OTP is invalid"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        otp.setVerified(true);
        otpRepo.saveAndFlush(otp);
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String confirmPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        UserAccount user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        PasswordResetOtp otp = otpRepo.findTopByUserAndVerifiedTrueAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("You must verify OTP first"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verified OTP has expired");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        System.out.println("===== RESET PASSWORD DEBUG =====");
        System.out.println("Email: " + user.getEmail());
        System.out.println("User ID: " + user.getId());
        System.out.println("Old hash: " + user.getPasswordHash());
        System.out.println("New hash: " + encodedPassword);

        user.setPasswordHash(encodedPassword);

        // reset xong thì cho phép login local luôn
        user.setAuthProvider("LOCAL");

        userRepo.saveAndFlush(user);

        otp.setUsed(true);
        otpRepo.saveAndFlush(otp);

        System.out.println("Saved password successfully.");
        System.out.println("Password matches new hash = " + passwordEncoder.matches(newPassword, encodedPassword));
        System.out.println("================================");
    }

    private String generateOtp() {
        int number = 100000 + new Random().nextInt(900000);
        return String.valueOf(number);
    }
}