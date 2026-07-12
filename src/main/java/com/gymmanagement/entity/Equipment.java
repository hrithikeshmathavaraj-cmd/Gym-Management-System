package com.gymmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * MongoDB document representing a piece of gym equipment and its maintenance status.
 */
@Document(collection = "equipment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    @Id
    private String id;

    @NotBlank(message = "Equipment name is required")
    private String name;

    private LocalDate purchaseDate;

    /** e.g. NEW, GOOD, FAIR, NEEDS_REPAIR */
    private String condition;

    /** e.g. AVAILABLE, IN_USE, UNDER_MAINTENANCE, RETIRED */
    private String status;

    private LocalDate maintenanceDate;
}
