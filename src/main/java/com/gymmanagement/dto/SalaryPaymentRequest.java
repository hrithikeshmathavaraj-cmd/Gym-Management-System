package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPaymentRequest {

    @NotBlank(message = "Trainer id is required")
    private String trainerId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Month is required")
    private String month;

    @NotBlank(message = "Payment method is required")
    private String method;

    private String notes;
}
