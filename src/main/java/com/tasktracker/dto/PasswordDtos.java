package com.tasktracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class PasswordDtos {

    @Getter @Setter
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Please enter a valid email address")
        private String email;
    }

    @Getter @Setter
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        private String password;

        @NotBlank(message = "Please confirm your password")
        private String confirmPassword;

        public boolean passwordsMatch() {
            return password != null && password.equals(confirmPassword);
        }
    }
}
