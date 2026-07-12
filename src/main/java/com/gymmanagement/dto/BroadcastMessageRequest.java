package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent by an admin to broadcast an announcement to every trainer, every
 * member, or everyone at once.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessageRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    /** ALL, TRAINER, or MEMBER */
    @NotBlank(message = "Target audience is required")
    private String audience;
}
