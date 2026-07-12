package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.entity.User;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for the currently authenticated user to view/update their own profile.
 * Accessible to any authenticated role (Admin, Receptionist, Trainer, Member).
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Current user's profile endpoints")
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping
    @Operation(summary = "Update the current authenticated user's profile (name, profile image)")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody User updateRequest) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updateRequest.getName() != null) {
            user.setName(updateRequest.getName());
        }
        if (updateRequest.getProfileImage() != null) {
            user.setProfileImage(updateRequest.getProfileImage());
        }

        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", saved));
    }
}
