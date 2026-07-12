package com.gymmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequest {

    @NotBlank(message = "Equipment name is required")
    private String name;

    private LocalDate purchaseDate;

    private String condition;

    private String status;

    private LocalDate maintenanceDate;
}
