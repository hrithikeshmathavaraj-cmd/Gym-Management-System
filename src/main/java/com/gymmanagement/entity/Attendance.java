package com.gymmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB document representing a single day's attendance record (QR check-in/check-out)
 * for a member.
 */
@Document(collection = "attendance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    private String id;

    @NotBlank(message = "Member id is required")
    private String memberId;

    @Builder.Default
    private LocalDate date = LocalDate.now();

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;
}
