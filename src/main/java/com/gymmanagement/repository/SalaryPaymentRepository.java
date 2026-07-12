package com.gymmanagement.repository;

import com.gymmanagement.entity.SalaryPayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryPaymentRepository extends MongoRepository<SalaryPayment, String> {
    List<SalaryPayment> findByTrainerIdOrderByPaymentDateDesc(String trainerId);
    List<SalaryPayment> findAllByOrderByPaymentDateDesc();
}
