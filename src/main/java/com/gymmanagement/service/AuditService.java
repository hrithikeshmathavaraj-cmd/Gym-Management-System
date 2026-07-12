package com.gymmanagement.service;

import com.gymmanagement.entity.LoginAudit;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuditService {

    void recordLoginSuccess(String username, String name, String role, HttpServletRequest request);

    void recordLoginFailed(String username, String reason, HttpServletRequest request);

    void recordLogout(String username, HttpServletRequest request);

    List<LoginAudit> getAllAudits();

    List<LoginAudit> getAuditsForUser(String username);
}
