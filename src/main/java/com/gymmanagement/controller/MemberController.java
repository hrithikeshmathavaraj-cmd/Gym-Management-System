package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.dto.MemberResponse;
import com.gymmanagement.dto.PasswordResetRequest;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for member CRUD, search/pagination, and membership renewal.
 * Restricted to ADMIN and RECEPTIONIST roles (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management endpoints")
public class MemberController {

    private final MemberService memberService;

    @Value("${app.default-member-password}")
    private String defaultMemberPassword;

    @PostMapping
    @Operation(summary = "Create a new member")
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.createMember(request);
        String message = String.format(
                "Member created successfully. Login ID: %s  Password: %s",
                response.getMemberCode(), defaultMemberPassword);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing member")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable String id, @Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a member")
    public ResponseEntity<ApiResponse<Object>> deleteMember(@PathVariable String id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success("Member deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member by id")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently logged-in member's own record (Login ID = memberCode)")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyMemberRecord(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberByCode(principal.getUsername())));
    }

    @GetMapping
    @Operation(summary = "Get all members with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(memberService.getAllMembers(pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members by name, member code or phone")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> searchMembers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(keyword, pageable)));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew a member's membership with a given plan")
    public ResponseEntity<ApiResponse<MemberResponse>> renewMembership(
            @PathVariable String id, @RequestParam String planId) {
        MemberResponse response = memberService.renewMembership(id, planId);
        return ResponseEntity.ok(ApiResponse.success("Membership renewed successfully", response));
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock a member's login (also happens automatically when their membership expires)")
    public ResponseEntity<ApiResponse<MemberResponse>> lockMember(@PathVariable String id) {
        MemberResponse response = memberService.setMemberLocked(id, true);
        return ResponseEntity.ok(ApiResponse.success("Member locked — they can no longer log in", response));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock a member's login and reactivate their membership status")
    public ResponseEntity<ApiResponse<MemberResponse>> unlockMember(@PathVariable String id) {
        MemberResponse response = memberService.setMemberLocked(id, false);
        return ResponseEntity.ok(ApiResponse.success("Member unlocked — they can log in again", response));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset a member login password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @PathVariable String id, @Valid @RequestBody PasswordResetRequest request) {
        memberService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
