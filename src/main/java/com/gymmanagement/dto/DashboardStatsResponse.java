package com.gymmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalMembers;
    private long activeMembers;
    private long expiredMembers;
    private long todaysAttendance;
    private double totalRevenue;
    private double monthlyRevenue;
    private long totalTrainers;
    private long totalEquipment;
    private List<PaymentResponse> recentPayments;
    private Map<String, Long> membershipDistribution;
    private Map<String, Double> monthlyRevenueChart;
    private Map<String, Long> attendanceChart;
}
