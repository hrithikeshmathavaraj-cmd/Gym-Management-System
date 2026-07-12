package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.SalaryPaymentRequest;
import com.gymmanagement.dto.SalaryPaymentResponse;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.TrainerRepository;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for paying and viewing trainer salaries. Admins pay and view
 * everyone's salary history; trainers can only view their own via /me.
 */
@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
@Tag(name = "Salary", description = "Trainer salary payment endpoints")
public class SalaryController {

    private final SalaryService salaryService;
    private final TrainerRepository trainerRepository;

    @PostMapping
    @Operation(summary = "Pay a trainer's salary for a given month")
    public ResponseEntity<ApiResponse<SalaryPaymentResponse>> paySalary(@Valid @RequestBody SalaryPaymentRequest request) {
        SalaryPaymentResponse response = salaryService.paySalary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Salary paid successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get every salary payment ever made (admin)")
    public ResponseEntity<ApiResponse<List<SalaryPaymentResponse>>> getAllSalaryPayments() {
        return ResponseEntity.ok(ApiResponse.success(salaryService.getAllSalaryPayments()));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get the salary payment history for one trainer (admin)")
    public ResponseEntity<ApiResponse<List<SalaryPaymentResponse>>> getSalaryForTrainer(@PathVariable String trainerId) {
        return ResponseEntity.ok(ApiResponse.success(salaryService.getSalaryPaymentsForTrainer(trainerId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently logged-in trainer's own salary history")
    public ResponseEntity<ApiResponse<List<SalaryPaymentResponse>>> getMySalary(@AuthenticationPrincipal UserPrincipal principal) {
        var trainer = trainerRepository.findByTrainerCode(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer record not found for this account"));
        return ResponseEntity.ok(ApiResponse.success(salaryService.getSalaryPaymentsForTrainer(trainer.getId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a salary payment record (admin)")
    public ResponseEntity<ApiResponse<Object>> deleteSalaryPayment(@PathVariable String id) {
        salaryService.deleteSalaryPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Salary payment deleted", null));
    }
}
