package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.AuthResponse;
import com.gymmanagement.dto.ForgotPasswordRequest;
import com.gymmanagement.dto.LoginRequest;
import com.gymmanagement.dto.RegisterRequest;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.MembershipStatus;
import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.User;
import com.gymmanagement.exception.DuplicateResourceException;
import com.gymmanagement.exception.InvalidCredentialsException;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.security.JwtTokenProvider;
import com.gymmanagement.service.AuditService;
import com.gymmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handles user registration, login and password reset, issuing JWTs on
 * successful authentication. Login is always by Login ID (username) — no email
 * address is used anywhere in this system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("This Login ID is already taken");
        }

        Role role = Role.MEMBER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getUsername(), saved.getRole());

        String token = jwtTokenProvider.generateToken(saved.getUsername(), saved.getRole().name(), saved.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(saved.getId())
                .name(saved.getName())
                .username(saved.getUsername())
                .role(saved.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String normalizedUsername = request.getUsername() == null ? "" : request.getUsername().trim();
        String normalizedPassword = request.getPassword() == null ? "" : request.getPassword().trim();

        User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElse(null);

        if (user == null || !passwordEncoder.matches(normalizedPassword, user.getPassword())) {
            auditService.recordLoginFailed(normalizedUsername, "Invalid Login ID or password", httpRequest);
            throw new InvalidCredentialsException("Invalid Login ID or password");
        }

        if (!user.isEnabled()) {
            auditService.recordLoginFailed(normalizedUsername, "Account disabled", httpRequest);
            throw new InvalidCredentialsException("This account has been locked. Please contact the front desk.");
        }

        // Safety net for members: if their membership expired since the last nightly
        // check, lock them out immediately rather than waiting for the 1am job.
        if (user.getRole() == Role.MEMBER) {
            Member member = memberRepository.findByMemberCode(user.getUsername()).orElse(null);
            if (member != null && member.getExpiryDate() != null && member.getExpiryDate().isBefore(LocalDate.now())
                    && member.getStatus() != MembershipStatus.EXPIRED) {
                member.setStatus(MembershipStatus.EXPIRED);
                memberRepository.save(member);
                user.setEnabled(false);
                userRepository.save(user);
                auditService.recordLoginFailed(normalizedUsername, "Membership expired", httpRequest);
                throw new InvalidCredentialsException("Your membership has expired. Please contact the front desk to renew.");
            }
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        log.info("User logged in: {}", user.getUsername());
        auditService.recordLoginSuccess(user.getUsername(), user.getName(), user.getRole().name(), httpRequest);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String username, HttpServletRequest httpRequest) {
        auditService.recordLogout(username, httpRequest);
        log.info("User logged out: {}", username);
    }

    @Override
    @Transactional
    public void resetPassword(ForgotPasswordRequest request) {
        String normalizedUsername = request.getUsername() == null ? "" : request.getUsername().trim();
        User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this Login ID"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getUsername());
    }
}
