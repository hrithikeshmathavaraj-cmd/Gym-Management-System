package com.gymmanagement.serviceImpl;

import com.gymmanagement.entity.AuditAction;
import com.gymmanagement.entity.LoginAudit;
import com.gymmanagement.repository.LoginAuditRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Records every login success, failed login attempt, and logout so admins can
 * see "who logged in, from where, and when" on the Reports page.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final LoginAuditRepository loginAuditRepository;
    private final UserRepository userRepository;

    @Override
    public void recordLoginSuccess(String username, String name, String role, HttpServletRequest request) {
        save(username, name, role, AuditAction.LOGIN_SUCCESS, null, request);
    }

    @Override
    public void recordLoginFailed(String username, String reason, HttpServletRequest request) {
        save(username, null, null, AuditAction.LOGIN_FAILED, reason, request);
    }

    @Override
    public void recordLogout(String username, HttpServletRequest request) {
        String name = null;
        String role = null;
        if (username != null) {
            var userOpt = userRepository.findByUsernameIgnoreCase(username.trim());
            if (userOpt.isPresent()) {
                name = userOpt.get().getName();
                role = userOpt.get().getRole().name();
            }
        }
        save(username, name, role, AuditAction.LOGOUT, null, request);
    }

    @Override
    public List<LoginAudit> getAllAudits() {
        return loginAuditRepository.findAllByOrderByTimestampDesc();
    }

    @Override
    public List<LoginAudit> getAuditsForUser(String username) {
        return loginAuditRepository.findByUsernameIgnoreCaseOrderByTimestampDesc(username);
    }

    private void save(String username, String name, String role, AuditAction action, String detail, HttpServletRequest request) {
        try {
            LoginAudit audit = LoginAudit.builder()
                    .username(username)
                    .name(name)
                    .role(role)
                    .action(action)
                    .detail(detail)
                    .ipAddress(extractClientIp(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();
            loginAuditRepository.save(audit);
        } catch (Exception ex) {
            // Auditing must never break login/logout itself (e.g. if Mongo hiccups).
            log.warn("Could not record audit entry for {} ({}): {}", username, action, ex.getMessage());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
