package com.gila.ecommerce.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"; // 512 bit secret
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    public void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    public void testValidateToken_InvalidSignature() {
        String token = jwtTokenProvider.generateToken("testuser");
        String tamperedToken = token + "extra";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    public void testValidateToken_Malformed() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.token"));
    }
}
