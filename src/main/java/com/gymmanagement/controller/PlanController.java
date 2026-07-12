package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.PlanRequest;
import com.gymmanagement.entity.Plan;
import com.gymmanagement.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for CRUD operations on membership plans.
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Membership plan management endpoints")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @Operation(summary = "Create a new membership plan")
    public ResponseEntity<ApiResponse<Plan>> createPlan(@Valid @RequestBody PlanRequest request) {
        Plan plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Plan created successfully", plan));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing plan")
    public ResponseEntity<ApiResponse<Plan>> updatePlan(@PathVariable String id, @Valid @RequestBody PlanRequest request) {
        Plan plan = planService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plan updated successfully", plan));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a plan")
    public ResponseEntity<ApiResponse<Object>> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Plan deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a plan by id")
    public ResponseEntity<ApiResponse<Plan>> getPlanById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all plans")
    public ResponseEntity<ApiResponse<List<Plan>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllPlans()));
    }
}
