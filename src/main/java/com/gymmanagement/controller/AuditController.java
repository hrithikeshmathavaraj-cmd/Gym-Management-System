package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.entity.LoginAudit;
import com.gymmanagement.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only endpoint exposing the login/logout audit trail shown on the
 * Reports page ("who logged in, from where, and when").
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Login/logout activity log")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logins")
    @Operation(summary = "Get the full login/logout activity log")
    public ResponseEntity<ApiResponse<List<LoginAudit>>> getAllAudits() {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAllAudits()));
    }
}
