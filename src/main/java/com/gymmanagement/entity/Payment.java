package com.gymmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing a payment/transaction made by a member.
 */
@Document(collection = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id;

    @NotBlank(message = "Member id is required")
    private String memberId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    /** e.g. CASH, CARD, UPI, NET_BANKING */
    private String paymentMethod;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.SUCCESS;

    private String transactionId;
}
