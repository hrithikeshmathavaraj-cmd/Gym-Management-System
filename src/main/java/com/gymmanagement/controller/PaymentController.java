package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.PaymentRequest;
import com.gymmanagement.dto.PaymentResponse;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.service.PaymentService;
import com.gymmanagement.util.QrCodeGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST endpoints for recording and querying member payments, plus generating
 * a UPI payment QR code so a member can pay via any UPI app at checkout.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberRepository memberRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.upi.id}")
    private String upiId;

    @Value("${app.upi.payee-name}")
    private String upiPayeeName;

    @Value("${app.upi.currency}")
    private String upiCurrency;

    @PostMapping
    @Operation(summary = "Record a new payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment recorded successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(@PathVariable String id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", paymentService.updatePayment(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment")
    public ResponseEntity<ApiResponse<Object>> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by id")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPayments()));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get payment history for a specific member")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByMember(@PathVariable String memberId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByMember(memberId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently logged-in member's own payment history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.gymmanagement.security.UserPrincipal principal) {
        var member = memberRepository.findByMemberCode(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Member record not found for this account"));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByMember(member.getId())));
    }

    @GetMapping("/upi-qrcode")
    @Operation(summary = "Generate a scannable UPI payment QR code for a member's due amount")
    public ResponseEntity<ApiResponse<String>> getUpiQrCode(
            @RequestParam String memberId,
            @RequestParam double amount) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        String note = URLEncoder.encode("Gym Membership Payment", StandardCharsets.UTF_8);
        String payeeName = URLEncoder.encode(upiPayeeName, StandardCharsets.UTF_8);

        // Standard UPI deep-link format understood by every UPI app (GPay, PhonePe, Paytm, BHIM, etc.)
        String upiUri = String.format("upi://pay?pa=%s&pn=%s&am=%.2f&cu=%s&tn=%s",
                upiId, payeeName, amount, upiCurrency, note);

        String qrCode = qrCodeGenerator.generateBase64QrCode(upiUri);

        return ResponseEntity.ok(ApiResponse.success(qrCode));
    }

    @GetMapping("/upi-details")
    @Operation(summary = "Get the gym's UPI payment id and payee name for display")
    public ResponseEntity<ApiResponse<Object>> getUpiDetails() {
        return ResponseEntity.ok(ApiResponse.success(new UpiDetails(upiId, upiPayeeName)));
    }

    private record UpiDetails(String upiId, String payeeName) {
    }
}

