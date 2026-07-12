package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.PasswordResetRequest;
import com.gymmanagement.dto.TrainerRequest;
import com.gymmanagement.entity.Trainer;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.TrainerRepository;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers", description = "Trainer management endpoints")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainerRepository trainerRepository;

    @Value("${app.default-trainer-password}")
    private String defaultTrainerPassword;

    @PostMapping
    @Operation(summary = "Add a new trainer")
    public ResponseEntity<ApiResponse<Trainer>> createTrainer(@Valid @RequestBody TrainerRequest request) {
        Trainer trainer = trainerService.createTrainer(request);
        String message = String.format(
                "Trainer added successfully. Login ID: %s  Password: %s",
                trainer.getTrainerCode(), defaultTrainerPassword);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, trainer));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing trainer")
    public ResponseEntity<ApiResponse<Trainer>> updateTrainer(@PathVariable String id, @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trainer updated successfully", trainerService.updateTrainer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a trainer")
    public ResponseEntity<ApiResponse<Object>> deleteTrainer(@PathVariable String id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.ok(ApiResponse.success("Trainer deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a trainer by id")
    public ResponseEntity<ApiResponse<Trainer>> getTrainerById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getTrainerById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all trainers")
    public ResponseEntity<ApiResponse<List<Trainer>>> getAllTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getAllTrainers()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get trainers currently marked as available (visible to members)")
    public ResponseEntity<ApiResponse<List<Trainer>>> getAvailableTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getAvailableTrainers()));
    }

    @PostMapping("/{id}/availability")
    @Operation(summary = "Mark a trainer as available or unavailable")
    public ResponseEntity<ApiResponse<Trainer>> setAvailability(@PathVariable String id, @RequestParam boolean available) {
        Trainer trainer = trainerService.setAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success(available ? "Trainer marked available" : "Trainer marked unavailable", trainer));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently logged-in trainer's own profile")
    public ResponseEntity<ApiResponse<Trainer>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Trainer trainer = trainerRepository.findByTrainerCode(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer record not found for this account"));
        return ResponseEntity.ok(ApiResponse.success(trainer));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset a trainer login password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @PathVariable String id, @Valid @RequestBody PasswordResetRequest request) {
        trainerService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
