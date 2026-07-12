package com.gymmanagement.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a gym trainer/instructor.
 */
@Document(collection = "trainers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {

    @Id
    private String id;

    /** Auto-generated, unique — also used as this trainer's Login ID. */
    private String trainerCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String specialization;

    @PositiveOrZero(message = "Salary cannot be negative")
    private double salary;

    /** Experience in years. */
    @PositiveOrZero(message = "Experience cannot be negative")
    private int experience;

    private String profileImage;

    /** Whether this trainer is currently available to take on members — toggled by admin. */
    @Builder.Default
    private boolean available = true;
}
