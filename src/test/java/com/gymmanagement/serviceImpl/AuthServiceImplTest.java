package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.AuthResponse;
import com.gymmanagement.dto.LoginRequest;
import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.User;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.security.JwtTokenProvider;
import com.gymmanagement.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void loginAcceptsTrimmedAndCaseInsensitiveUsername() {
        UserRepository userRepository = mock(UserRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        AuditService auditService = mock(AuditService.class);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        AuthServiceImpl service = new AuthServiceImpl(userRepository, memberRepository, passwordEncoder, jwtTokenProvider, auditService);

        User user = User.builder()
                .id("user-1")
                .name("Administrator")
                .username("admin")
                .password("encoded")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken("admin", "ADMIN", "user-1")).thenReturn("token-123");

        AuthResponse response = service.login(new LoginRequest("  admin  ", "admin123"), httpRequest);

        assertEquals("ADMIN", response.getRole());
        assertEquals("token-123", response.getToken());
    }
}
