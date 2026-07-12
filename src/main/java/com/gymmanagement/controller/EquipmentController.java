package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.EquipmentRequest;
import com.gymmanagement.entity.Equipment;
import com.gymmanagement.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Gym equipment inventory management endpoints")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @Operation(summary = "Add new equipment")
    public ResponseEntity<ApiResponse<Equipment>> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        Equipment equipment = equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Equipment added successfully", equipment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing equipment")
    public ResponseEntity<ApiResponse<Equipment>> updateEquipment(@PathVariable String id, @Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment updated successfully", equipmentService.updateEquipment(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment")
    public ResponseEntity<ApiResponse<Object>> deleteEquipment(@PathVariable String id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by id")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.getEquipmentById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all equipment")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.getAllEquipment()));
    }
}
