package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;
}
