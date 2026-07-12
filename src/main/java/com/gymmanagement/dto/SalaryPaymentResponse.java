package com.gymmanagement.dto;

import com.gymmanagement.entity.SalaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPaymentResponse {
    private String id;
    private String trainerId;
    private String trainerName;
    private double amount;
    private String month;
    private LocalDateTime paymentDate;
    private String method;
    private SalaryStatus status;
    private String notes;
}
