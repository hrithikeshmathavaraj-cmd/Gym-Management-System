package com.gymmanagement.dto;

import com.gymmanagement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String memberId;
    private String memberName;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionId;
}
