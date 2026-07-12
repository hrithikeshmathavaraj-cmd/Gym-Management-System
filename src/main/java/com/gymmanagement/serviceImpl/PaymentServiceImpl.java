package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.PaymentRequest;
import com.gymmanagement.dto.PaymentResponse;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.Payment;
import com.gymmanagement.entity.PaymentStatus;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.PaymentRepository;
import com.gymmanagement.service.PaymentService;
import com.gymmanagement.service.realtime.RealtimeDashboardPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for recording and querying member payments, and computing
 * revenue figures used by the reporting/dashboard module.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RealtimeDashboardPublisher realtimeDashboardPublisher;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        Payment payment = Payment.builder()
                .memberId(request.getMemberId())
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .transactionId(request.getTransactionId() != null ? request.getTransactionId() : UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment recorded: {} for member {}", saved.getId(), member.getName());
        realtimeDashboardPublisher.publish("PAYMENT_CREATED");

        return toResponse(saved, member.getName());
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(String id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        payment.setMemberId(request.getMemberId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved, member.getName());
    }

    @Override
    @Transactional
    public void deletePayment(String id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
        log.info("Payment deleted: {}", id);
        realtimeDashboardPublisher.publish("PAYMENT_DELETED");
    }

    @Override
    public PaymentResponse getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        String memberName = memberRepository.findById(payment.getMemberId()).map(Member::getName).orElse("Unknown");
        return toResponse(payment, memberName);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(p -> toResponse(p, resolveMemberName(p.getMemberId())))
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByMember(String memberId) {
        return paymentRepository.findByMemberId(memberId).stream()
                .map(p -> toResponse(p, resolveMemberName(p.getMemberId())))
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsBetween(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByPaymentDateBetween(start, end).stream()
                .map(p -> toResponse(p, resolveMemberName(p.getMemberId())))
                .toList();
    }

    @Override
    public double getTotalRevenue() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    @Override
    public double getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByPaymentDateBetween(start, end).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    private String resolveMemberName(String memberId) {
        return memberRepository.findById(memberId).map(Member::getName).orElse("Unknown");
    }

    private PaymentResponse toResponse(Payment payment, String memberName) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .memberId(payment.getMemberId())
                .memberName(memberName)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .build();
    }
}
