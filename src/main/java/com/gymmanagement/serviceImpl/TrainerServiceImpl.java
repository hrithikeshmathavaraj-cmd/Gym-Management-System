package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.TrainerRequest;
import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.Trainer;
import com.gymmanagement.entity.User;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.TrainerRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.service.TrainerService;
import com.gymmanagement.util.TrainerCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing gym trainers. Creating a trainer also
 * auto-provisions a login account: the generated trainerCode becomes their
 * Login ID, paired with the configured default trainer password, so they can
 * sign in immediately — no email address involved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrainerCodeGenerator trainerCodeGenerator;

    @Value("${app.default-trainer-password}")
    private String defaultTrainerPassword;

    @Override
    @Transactional
    public Trainer createTrainer(TrainerRequest request) {
        String trainerCode = trainerCodeGenerator.generate();

        Trainer trainer = Trainer.builder()
                .trainerCode(trainerCode)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .specialization(request.getSpecialization())
                .salary(request.getSalary())
                .experience(request.getExperience())
                .build();
        Trainer saved = trainerRepository.save(trainer);

        // Auto-provision a login account for this trainer using their code as the Login ID.
        User loginAccount = User.builder()
                .name(saved.getName())
                .username(saved.getTrainerCode())
                .password(passwordEncoder.encode(defaultTrainerPassword))
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        userRepository.save(loginAccount);

        log.info("Trainer created: {} (Login ID: {})", saved.getName(), saved.getTrainerCode());
        return saved;
    }

    @Override
    @Transactional
    public Trainer updateTrainer(String id, TrainerRequest request) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));

        trainer.setName(request.getName());
        trainer.setPhone(request.getPhone());
        trainer.setEmail(request.getEmail());
        trainer.setSpecialization(request.getSpecialization());
        trainer.setSalary(request.getSalary());
        trainer.setExperience(request.getExperience());

        Trainer saved = trainerRepository.save(trainer);

        // Keep the linked login account's display name in sync.
        userRepository.findByUsername(saved.getTrainerCode()).ifPresent(user -> {
            user.setName(saved.getName());
            userRepository.save(user);
        });

        log.info("Trainer updated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteTrainer(String id) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));

        trainerRepository.deleteById(id);
        userRepository.findByUsername(trainer.getTrainerCode()).ifPresent(userRepository::delete);
        log.info("Trainer deleted: {}", id);
    }

    @Override
    public Trainer getTrainerById(String id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
    }

    @Override
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    @Override
    public List<Trainer> getAvailableTrainers() {
        return trainerRepository.findAll().stream()
                .filter(Trainer::isAvailable)
                .toList();
    }

    @Override
    @Transactional
    public Trainer setAvailability(String id, boolean available) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
        trainer.setAvailable(available);
        Trainer saved = trainerRepository.save(trainer);
        log.info("Trainer {} marked {}", saved.getName(), available ? "AVAILABLE" : "UNAVAILABLE");
        return saved;
    }

    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));

        User user = userRepository.findByUsername(trainer.getTrainerCode())
                .orElseThrow(() -> new ResourceNotFoundException("No login account found for this trainer"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for trainer: {}", trainer.getTrainerCode());
    }
}
