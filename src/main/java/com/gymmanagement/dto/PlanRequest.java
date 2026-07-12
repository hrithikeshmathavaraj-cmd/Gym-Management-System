package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    private String planName;

    @Positive(message = "Duration must be positive")
    private int duration;

    @Positive(message = "Price must be positive")
    private double price;

    private String description;
}
