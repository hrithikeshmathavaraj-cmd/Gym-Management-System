package com.gymmanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security configuration: stateless JWT auth, BCrypt password
 * hashing, CORS, and role-based authorization rules per endpoint group.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**", "/images/**", "/fonts/**", "/dashboard.html", "/member-dashboard.html", "/trainer-dashboard.html", "/members.html", "/plans.html", "/payments.html", "/attendance.html", "/trainers.html", "/equipment.html", "/reports.html", "/notifications.html", "/settings.html", "/profile.html").permitAll()
                .requestMatchers("/favicon.ico", "/favicon.png", "/favicon.svg", "/favicon-16.png", "/favicon-32.png", "/apple-touch-icon.png", "/logo.png").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // A member's own record and own payment history — must come before the
                // broader /api/members/** and /api/payments/** admin-only rules below.
                .requestMatchers("/api/members/me").hasAnyRole("MEMBER", "ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/payments/me").hasAnyRole("MEMBER", "ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/attendance/me").hasAnyRole("MEMBER", "ADMIN", "RECEPTIONIST")
                // A trainer's own profile and own salary history — must come before the
                // admin-only /api/trainers/** and /api/salary/** rules below.
                .requestMatchers("/api/trainers/me").hasAnyRole("TRAINER", "ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/trainers/available").hasAnyRole("MEMBER", "TRAINER", "ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/salary/me").hasAnyRole("TRAINER", "ADMIN")
                .requestMatchers("/api/reports/export/salary/*/slip").hasAnyRole("TRAINER", "ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/reports/export/payments/*/receipt").hasAnyRole("MEMBER", "ADMIN", "RECEPTIONIST")
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/notifications/broadcast").hasRole("ADMIN")
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                .requestMatchers("/api/salary/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/trainers/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/equipment/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                // Admin + Receptionist can manage members/plans/payments/attendance
                .requestMatchers("/api/members/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/plans/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER")
                // Any authenticated user (including MEMBER) can access their own profile/notifications
                .requestMatchers("/api/profile/**", "/api/notifications/**").authenticated()
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
