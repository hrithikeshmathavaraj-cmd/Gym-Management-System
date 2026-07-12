package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.DashboardStatsResponse;
import com.gymmanagement.entity.Member;
import com.gymmanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints powering the dashboard: aggregate stats, charts, and
 * member expiry/active reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Dashboard analytics and reporting endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregate dashboard statistics and chart data")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboardStats()));
    }

    @GetMapping("/members/expired")
    @Operation(summary = "Get all expired members")
    public ResponseEntity<ApiResponse<List<Member>>> getExpiredMembers() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getExpiredMembers()));
    }

    @GetMapping("/members/active")
    @Operation(summary = "Get all active members")
    public ResponseEntity<ApiResponse<List<Member>>> getActiveMembers() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getActiveMembers()));
    }

    @GetMapping("/members/expiring")
    @Operation(summary = "Get members expiring within N days")
    public ResponseEntity<ApiResponse<List<Member>>> getExpiringMembers(@RequestParam(defaultValue = "7") int withinDays) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getExpiringMembers(withinDays)));
    }
}
