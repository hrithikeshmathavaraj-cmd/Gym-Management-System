package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.BroadcastMessageRequest;
import com.gymmanagement.entity.Notification;
import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.User;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.NotificationRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Endpoints for the current user to view and mark notifications as read
 * (e.g. membership expiry reminders, payment due alerts), and for admins to
 * broadcast an announcement to all trainers, all members, or everyone.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification endpoints")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(principal.getId())));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications for the current user")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByRecipientIdAndReadFalse(principal.getId())));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        return ResponseEntity.ok(ApiResponse.success(notificationRepository.save(notification)));
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Admin: send an announcement to all trainers, all members, or everyone")
    public ResponseEntity<ApiResponse<Object>> broadcast(@Valid @RequestBody BroadcastMessageRequest request) {
        List<User> recipients = new ArrayList<>();
        String audience = request.getAudience() == null ? "ALL" : request.getAudience().toUpperCase();

        if (audience.equals("TRAINER") || audience.equals("ALL")) {
            recipients.addAll(userRepository.findByRole(Role.TRAINER));
        }
        if (audience.equals("MEMBER") || audience.equals("ALL")) {
            recipients.addAll(userRepository.findByRole(Role.MEMBER));
        }

        List<Notification> notifications = recipients.stream()
                .map(user -> Notification.builder()
                        .recipientId(user.getId())
                        .recipientType(user.getRole().name())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type("ANNOUNCEMENT")
                        .read(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok(ApiResponse.success(
                "Sent to " + notifications.size() + " recipient" + (notifications.size() == 1 ? "" : "s"), null));
    }
}
