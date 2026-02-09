package com.example.hrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Vui lòng nhập họ tên.")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập username.")
    private String username;

    @Email(message = "Email không hợp lệ.")
    @NotBlank(message = "Vui lòng nhập email.")
    private String email;

    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu.")
    private String confirmPassword;
}
