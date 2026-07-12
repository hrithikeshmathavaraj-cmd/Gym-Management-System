package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Login ID is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,40}$", message = "Login ID must be 3-40 characters (letters, numbers, dot, dash, underscore only)")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Optional - defaults to MEMBER if not provided. Only an ADMIN can create other roles. */
    private String role;
}
