package com.example.hrm.service;

import com.example.hrm.dto.LoginRequest;
import com.example.hrm.dto.RegisterRequest;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.User;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final EmployeeRepository empRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepo, EmployeeRepository empRepo) {
        this.userRepo = userRepo;
        this.empRepo = empRepo;
    }

    // ================= LOGIN =================
    public void login(LoginRequest req, HttpSession session) {

        User user = req.getUsernameOrEmail().contains("@")
                ? userRepo.findByEmailIgnoreCase(req.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"))
                : userRepo.findByUsernameIgnoreCase(req.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Username không tồn tại"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        Employee emp = empRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User chưa có Employee"));

        // session cho interceptor
        session.setAttribute("EMP_ID", emp.getEmpId());
        session.setAttribute("USER_ID", user.getUserId());
        session.setAttribute("USERNAME", user.getUsername());
        session.setAttribute("ROLE_ID", user.getRoleId());
    }

    // ================= REGISTER =================
    public void register(RegisterRequest req) {

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        if (userRepo.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(req.getUsername().trim())
                .email(req.getEmail().trim().toLowerCase())
                .passwordHash(encoder.encode(req.getPassword()))
                .isActive(true)
                .build();

        user = userRepo.save(user);

        // tạo employee tối thiểu
        Employee emp = new Employee();
        emp.setUserId(user.getUserId());
        emp.setFullName(req.getFullName());
        emp.setStatus("PROBATION");
        empRepo.save(emp);
    }
}
