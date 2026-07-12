package com.gymmanagement.service;

import com.gymmanagement.dto.SalaryPaymentRequest;
import com.gymmanagement.dto.SalaryPaymentResponse;

import java.util.List;

public interface SalaryService {
    SalaryPaymentResponse paySalary(SalaryPaymentRequest request);
    List<SalaryPaymentResponse> getAllSalaryPayments();
    List<SalaryPaymentResponse> getSalaryPaymentsForTrainer(String trainerId);
    SalaryPaymentResponse getSalaryPaymentById(String id);
    void deleteSalaryPayment(String id);
}
