package com.gymmanagement.dto;

import com.gymmanagement.entity.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String id;
    private String memberCode;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String membershipPlan;
    private String membershipPlanName;
    private LocalDate joinDate;
    private LocalDate expiryDate;
    private MembershipStatus status;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
