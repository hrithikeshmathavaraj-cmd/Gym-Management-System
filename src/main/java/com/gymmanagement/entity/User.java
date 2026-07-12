package com.gymmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing an application user (login/auth identity).
 * Every actor in the system (admin, receptionist, trainer, member) has a
 * corresponding User record used purely for authentication and authorization.
 *
 * Login is by {@code username} (a Login ID) — never email. For Admin/Receptionist
 * accounts the username is chosen at registration (e.g. "admin"). For Members and
 * Trainers, the username is auto-set to their generated member/trainer code when
 * their record is created, so they can log in with that code + a default password.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    /** The Login ID used to sign in — e.g. "admin", a member code, or a trainer code. */
    @Indexed(unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private Role role = Role.MEMBER;

    private String profileImage;

    @Builder.Default
    private boolean enabled = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

