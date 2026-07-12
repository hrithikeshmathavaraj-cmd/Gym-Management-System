package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.entity.Attendance;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for QR-based attendance check-in/check-out and attendance reporting.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "QR check-in/check-out and attendance reporting endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberRepository memberRepository;

    @PostMapping("/check-in")
    @Operation(summary = "Check in a member via QR code scan")
    public ResponseEntity<ApiResponse<Attendance>> checkIn(@RequestParam String memberId) {
        Attendance attendance = attendanceService.checkIn(memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Checked in successfully", attendance));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out a member via QR code scan")
    public ResponseEntity<ApiResponse<Attendance>> checkOut(@RequestParam String memberId) {
        Attendance attendance = attendanceService.checkOut(memberId);
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", attendance));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's attendance records")
    public ResponseEntity<ApiResponse<List<Attendance>>> getTodaysAttendance() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodaysAttendance()));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get attendance records for a given month")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMonthlyAttendance(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMonthlyAttendance(year, month)));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get attendance history for a specific member")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceByMember(@PathVariable String memberId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByMember(memberId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently logged-in member's own attendance history")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMyAttendance(@AuthenticationPrincipal UserPrincipal principal) {
        var member = memberRepository.findByMemberCode(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Member record not found for this account"));
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByMember(member.getId())));
    }
}
