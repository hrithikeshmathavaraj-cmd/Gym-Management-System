package com.gymmanagement.repository;

import com.gymmanagement.entity.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerRepository extends MongoRepository<Trainer, String> {
    Optional<Trainer> findByTrainerCode(String trainerCode);
}
