package com.gymmanagement.service;

import com.gymmanagement.dto.AuthResponse;
import com.gymmanagement.dto.ForgotPasswordRequest;
import com.gymmanagement.dto.LoginRequest;
import com.gymmanagement.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);
    void logout(String username, HttpServletRequest httpRequest);
    void resetPassword(ForgotPasswordRequest request);
}
