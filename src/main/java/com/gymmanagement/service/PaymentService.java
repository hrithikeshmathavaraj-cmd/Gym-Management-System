package com.gymmanagement.service;

import com.gymmanagement.dto.PaymentRequest;
import com.gymmanagement.dto.PaymentResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse updatePayment(String id, PaymentRequest request);
    void deletePayment(String id);
    PaymentResponse getPaymentById(String id);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByMember(String memberId);
    List<PaymentResponse> getPaymentsBetween(LocalDateTime start, LocalDateTime end);
    double getTotalRevenue();
    double getRevenueBetween(LocalDateTime start, LocalDateTime end);
}
