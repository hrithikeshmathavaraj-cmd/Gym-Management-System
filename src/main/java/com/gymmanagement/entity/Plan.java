package com.gymmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a membership plan (e.g. Monthly, Quarterly, Annual).
 */
@Document(collection = "plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    private String id;

    @NotBlank(message = "Plan name is required")
    private String planName;

    /** Duration of the plan in days. */
    @Positive(message = "Duration must be positive")
    private int duration;

    @Positive(message = "Price must be positive")
    private double price;

    private String description;

    @Builder.Default
    private boolean active = true;
}
