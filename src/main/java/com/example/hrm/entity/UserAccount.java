package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // Cho phép null để hỗ trợ account đăng nhập bằng Google
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ===== Thêm mới để hỗ trợ Google Login =====
    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    @Column(name = "auth_provider", length = 20)
    private String authProvider; // LOCAL / GOOGLE
}