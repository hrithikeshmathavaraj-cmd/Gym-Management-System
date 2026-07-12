package com.gymmanagement.service;

import com.gymmanagement.dto.TrainerRequest;
import com.gymmanagement.entity.Trainer;

import java.util.List;

public interface TrainerService {

    Trainer createTrainer(TrainerRequest request);

    Trainer updateTrainer(String id, TrainerRequest request);

    void deleteTrainer(String id);

    Trainer getTrainerById(String id);

    List<Trainer> getAllTrainers();

    List<Trainer> getAvailableTrainers();

    Trainer setAvailability(String id, boolean available);

    void resetPassword(String id, String newPassword);
}
