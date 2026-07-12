package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.entity.Settings;
import com.gymmanagement.repository.SettingsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints for viewing/updating global gym settings
 * (gym name, contact info, expiry reminder threshold, theme).
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin - Settings", description = "Global application settings (Admin only)")
public class SettingsController {

    private final SettingsRepository settingsRepository;

    @GetMapping
    @Operation(summary = "Get current application settings")
    public ResponseEntity<ApiResponse<Settings>> getSettings() {
        Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> settingsRepository.save(Settings.builder().build()));
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    @Operation(summary = "Update application settings")
    public ResponseEntity<ApiResponse<Settings>> updateSettings(@RequestBody Settings updated) {
        Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> Settings.builder().build());

        settings.setGymName(updated.getGymName());
        settings.setGymAddress(updated.getGymAddress());
        settings.setGymContact(updated.getGymContact());
        settings.setGymEmail(updated.getGymEmail());
        settings.setExpiryReminderDays(updated.getExpiryReminderDays());
        settings.setTheme(updated.getTheme());

        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", settingsRepository.save(settings)));
    }
}
