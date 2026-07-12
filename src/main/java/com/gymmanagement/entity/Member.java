package com.gymmanagement.entity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB document representing a gym member and their membership details.
 */
@Document(collection = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    private String id;

    @Indexed(unique = true)
    private String memberCode;

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 100, message = "Age must be realistic")
    private int age;

    private String gender;

    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone number must be valid")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String address;

    /** Reference to the Plan document id currently assigned to this member. */
    private String membershipPlan;

    @Field(name = "join_date")
    private LocalDate joinDate;

    @Field(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    private String profileImage;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
