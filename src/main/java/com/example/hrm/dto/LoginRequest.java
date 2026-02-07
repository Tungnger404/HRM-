package com.example.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Vui lòng nhập username hoặc email.")
    private String usernameOrEmail;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    private String password;
}
