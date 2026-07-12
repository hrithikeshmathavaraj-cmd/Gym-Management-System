package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Member id is required")
    private String memberId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String transactionId;
}
