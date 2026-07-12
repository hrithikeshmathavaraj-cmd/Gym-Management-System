package com.gymmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmanagement.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Intercepts every request once, extracts the JWT from the Authorization header,
 * validates it, and (if valid) populates the SecurityContext with the authenticated user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<AntPathRequestMatcher> PUBLIC_MATCHERS = List.of(
            new AntPathRequestMatcher("/api/auth/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/api-docs/**"),
            new AntPathRequestMatcher("/uploads/**"),
            new AntPathRequestMatcher("/ws/**"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/fonts/**"),
            new AntPathRequestMatcher("/*.html"),
            new AntPathRequestMatcher("/*.png"),
            new AntPathRequestMatcher("/*.svg"),
            new AntPathRequestMatcher("/*.ico")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException ex) {
            // Token is well-formed and unexpired, but the account it points to no longer
            // exists (e.g. deleted). This is a genuine auth failure -> 401.
            log.warn("Token references a user that no longer exists: {}", ex.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        } catch (DataAccessException ex) {
            // The token itself was fine — the failure happened while looking the user up,
            // almost always because MongoDB is unreachable/down. Do NOT report this as
            // "Unauthorized"/"Session expired", or every logged-in user gets bounced to the
            // login page whenever the database hiccups, which hides the real problem.
            log.error("Database unavailable while authenticating request: {}", ex.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Cannot reach the database right now. Please make sure MongoDB is running and try again.");
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_MATCHERS.stream().anyMatch(matcher -> matcher.matches(request));
    }
}
