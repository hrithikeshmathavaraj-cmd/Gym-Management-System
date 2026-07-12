package com.gymmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 100, message = "Age must be realistic")
    private int age;

    private String gender;

    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone number must be valid")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String address;

    @NotBlank(message = "Membership plan is required")
    private String membershipPlan;

    private LocalDate joinDate;
}
