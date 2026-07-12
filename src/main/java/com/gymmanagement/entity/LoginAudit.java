package com.gymmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document recording every login/logout event in the system, shown
 * to admins as a "Who's logging in" audit trail and exportable to PDF/Excel.
 */
@Document(collection = "login_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAudit {

    @Id
    private String id;

    /** Login ID (username) of the account involved — may not resolve to a user if login failed. */
    private String username;

    private String name;

    /** Null when the login attempt failed before a role could be resolved. */
    private String role;

    private AuditAction action;

    /** Extra context, e.g. "Invalid password" for a failed login. */
    private String detail;

    private String ipAddress;

    private String userAgent;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
