package com.gymmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent by the frontend when a user clicks "Logout", purely so the server can
 * record it in the login audit trail (JWTs are stateless, so there is nothing
 * to invalidate server-side — this call is for audit-logging purposes only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    private String username;
}
