package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.SalaryPaymentRequest;
import com.gymmanagement.dto.SalaryPaymentResponse;
import com.gymmanagement.entity.SalaryPayment;
import com.gymmanagement.entity.SalaryStatus;
import com.gymmanagement.entity.Trainer;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.SalaryPaymentRepository;
import com.gymmanagement.repository.TrainerRepository;
import com.gymmanagement.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for paying and tracking trainer salaries. Every payout is
 * stored permanently in MongoDB and immediately visible to both the admin
 * (Trainers page) and the trainer themselves (their own dashboard).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final TrainerRepository trainerRepository;

    @Override
    @Transactional
    public SalaryPaymentResponse paySalary(SalaryPaymentRequest request) {
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + request.getTrainerId()));

        SalaryPayment payment = SalaryPayment.builder()
                .trainerId(trainer.getId())
                .trainerName(trainer.getName())
                .amount(request.getAmount())
                .month(request.getMonth())
                .method(request.getMethod())
                .notes(request.getNotes())
                .status(SalaryStatus.PAID)
                .build();

        SalaryPayment saved = salaryPaymentRepository.save(payment);
        log.info("Salary paid to trainer {} for {}: {}", trainer.getName(), request.getMonth(), request.getAmount());
        return toResponse(saved);
    }

    @Override
    public List<SalaryPaymentResponse> getAllSalaryPayments() {
        return salaryPaymentRepository.findAllByOrderByPaymentDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SalaryPaymentResponse> getSalaryPaymentsForTrainer(String trainerId) {
        return salaryPaymentRepository.findByTrainerIdOrderByPaymentDateDesc(trainerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SalaryPaymentResponse getSalaryPaymentById(String id) {
        return salaryPaymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteSalaryPayment(String id) {
        if (!salaryPaymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Salary payment not found with id: " + id);
        }
        salaryPaymentRepository.deleteById(id);
    }

    private SalaryPaymentResponse toResponse(SalaryPayment payment) {
        return SalaryPaymentResponse.builder()
                .id(payment.getId())
                .trainerId(payment.getTrainerId())
                .trainerName(payment.getTrainerName())
                .amount(payment.getAmount())
                .month(payment.getMonth())
                .paymentDate(payment.getPaymentDate())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .build();
    }
}
