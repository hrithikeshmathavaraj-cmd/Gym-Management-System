package com.gymmanagement.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    @Test
    void generateTokenShouldWorkWithShortSecret() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86400000L);

        assertDoesNotThrow(() -> provider.generateToken("admin", "ADMIN", "user-1"));
    }
}
