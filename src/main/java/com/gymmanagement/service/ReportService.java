package com.gymmanagement.service;

import com.gymmanagement.dto.DashboardStatsResponse;
import com.gymmanagement.entity.Member;

import java.util.List;

public interface ReportService {
    DashboardStatsResponse getDashboardStats();
    List<Member> getExpiredMembers();
    List<Member> getActiveMembers();
    List<Member> getExpiringMembers(int withinDays);
}
