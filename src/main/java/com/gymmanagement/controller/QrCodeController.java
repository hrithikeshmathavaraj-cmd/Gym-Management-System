package com.gymmanagement.controller;

import com.gymmanagement.dto.ApiResponse;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.util.QrCodeGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates a scannable QR code (Base64 PNG) for a member's id, used to
 * drive attendance check-in/check-out.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member QR code endpoint")
public class QrCodeController {

    private final MemberRepository memberRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @GetMapping("/{id}/qrcode")
    @Operation(summary = "Generate a member's check-in QR code")
    public ResponseEntity<ApiResponse<String>> getMemberQrCode(@PathVariable String id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
        String qrCode = qrCodeGenerator.generateBase64QrCode(id);
        return ResponseEntity.ok(ApiResponse.success(qrCode));
    }
}
