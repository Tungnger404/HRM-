package com.example.hrm.repository;

import com.example.hrm.entity.PasswordResetOtp;
import com.example.hrm.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Integer> {

    Optional<PasswordResetOtp> findTopByUserAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(UserAccount user, String otpCode);

    Optional<PasswordResetOtp> findTopByUserAndVerifiedTrueAndUsedFalseOrderByCreatedAtDesc(UserAccount user);

    void deleteByExpiresAtBefore(LocalDateTime time);
}