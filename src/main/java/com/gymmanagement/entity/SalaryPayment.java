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
 * MongoDB document representing one salary payout made to a trainer for a
 * given month. History of these records is what powers both the admin's
 * "Trainer Salary" report and the trainer's own "My Salary" view.
 */
@Document(collection = "salary_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPayment {

    @Id
    private String id;

    @NotBlank(message = "Trainer id is required")
    private String trainerId;

    private String trainerName;

    @Positive(message = "Amount must be positive")
    private double amount;

    /** The salary month this payment covers, e.g. "2026-07". */
    @NotBlank(message = "Month is required")
    private String month;

    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    /** e.g. CASH, BANK_TRANSFER, UPI */
    private String method;

    @Builder.Default
    private SalaryStatus status = SalaryStatus.PAID;

    private String notes;
}
