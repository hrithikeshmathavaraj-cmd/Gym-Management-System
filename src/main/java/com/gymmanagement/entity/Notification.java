package com.gymmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing an in-app or email notification
 * (e.g. membership expiry reminder) sent to a user or member.
 */
@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    /** Recipient - either a User id or Member id depending on `recipientType`. */
    private String recipientId;

    private String recipientType;

    private String title;

    private String message;

    /** e.g. MEMBERSHIP_EXPIRY, PAYMENT_DUE, GENERAL */
    private String type;

    @Builder.Default
    private boolean read = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
