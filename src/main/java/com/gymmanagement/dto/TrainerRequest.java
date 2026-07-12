package com.gymmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String specialization;

    @PositiveOrZero(message = "Salary cannot be negative")
    private double salary;

    @PositiveOrZero(message = "Experience cannot be negative")
    private int experience;
}
