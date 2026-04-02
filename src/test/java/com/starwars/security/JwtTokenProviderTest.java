package com.starwars.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    // Same key used in application-test.yml
    private static final String SECRET =
            "dGVzdFNlY3JldEtleUZvclN0YXJXYXJzVGVzdGluZ09ubHkxMjM0NTY3ODk=";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretBase64", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMs", EXPIRATION_MS);

        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken returns a non-blank JWT string")
    void generateToken_returnsNonBlankToken() {
        String token = jwtTokenProvider.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername returns the username embedded in the token")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken(userDetails);
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("extractExpiration returns a future date")
    void extractExpiration_returnsFutureDate() {
        String token = jwtTokenProvider.generateToken(userDetails);
        Date expiry = jwtTokenProvider.extractExpiration(token);
        assertThat(expiry).isAfter(new Date());
    }

    @Test
    @DisplayName("isTokenValid returns true for a valid token and matching user")
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtTokenProvider.generateToken(userDetails);
        assertThat(jwtTokenProvider.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false when the token belongs to a different user")
    void isTokenValid_returnsFalseForDifferentUser() {
        String token = jwtTokenProvider.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("other").password("x")
                .authorities(Collections.emptyList()).build();
        assertThat(jwtTokenProvider.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for an expired token")
    void isTokenValid_returnsFalseForExpiredToken() {
        // Override expiration to 0ms so the token is immediately expired
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMs", 0L);
        String token = jwtTokenProvider.generateToken(userDetails);
        assertThat(jwtTokenProvider.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for a tampered token")
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtTokenProvider.generateToken(userDetails);
        String tampered = token + "tampered";
        assertThat(jwtTokenProvider.isTokenValid(tampered, userDetails)).isFalse();
    }
}