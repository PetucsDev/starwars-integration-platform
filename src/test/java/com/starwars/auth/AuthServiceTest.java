package com.starwars.auth;

import com.starwars.auth.dto.AuthResponse;
import com.starwars.auth.dto.LoginRequest;
import com.starwars.auth.dto.RegisterRequest;
import com.starwars.auth.entity.AppUser;
import com.starwars.auth.repository.UserRepository;
import com.starwars.auth.service.AuthService;
import com.starwars.exception.ConflictException;
import com.starwars.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, authenticationManager);
    }

    @Test
    @DisplayName("register with valid data saves user and returns a token")
    void register_withValidData_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("jedi", "force123", "jedi@galaxy.com");

        when(userRepository.existsByUsername("jedi")).thenReturn(false);
        when(userRepository.existsByEmail("jedi@galaxy.com")).thenReturn(false);
        when(passwordEncoder.encode("force123")).thenReturn("$2a$hashed");

        UserDetails userDetails = User.withUsername("jedi").password("$2a$hashed")
                .authorities(Collections.emptyList()).build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtTokenProvider.extractExpiration("jwt-token")).thenReturn(new Date());

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("jedi");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    @DisplayName("register with duplicate username throws ConflictException")
    void register_withDuplicateUsername_throwsConflict() {
        RegisterRequest request = new RegisterRequest("taken", "pass123", "new@email.com");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register with duplicate email throws ConflictException")
    void register_withDuplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest("newuser", "pass123", "existing@email.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existing@email.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login with valid credentials returns a token")
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("jedi", "force123");

        UserDetails userDetails = User.withUsername("jedi").password("$2a$hashed")
                .authorities(Collections.emptyList()).build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtTokenProvider.extractExpiration("jwt-token")).thenReturn(new Date());

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("jedi");
    }

    @Test
    @DisplayName("login with wrong credentials propagates BadCredentialsException")
    void login_withWrongCredentials_propagatesBadCredentials() {
        LoginRequest request = new LoginRequest("jedi", "wrongpass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("register does not encode the password if username is already taken")
    void register_withDuplicateUsername_neverEncodesPassword() {
        RegisterRequest request = new RegisterRequest("taken", "pass123", "email@test.com");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(passwordEncoder, never()).encode(anyString());
    }
}
