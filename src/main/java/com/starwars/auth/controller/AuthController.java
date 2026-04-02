package com.starwars.auth.controller;

import com.starwars.auth.dto.AuthResponse;
import com.starwars.auth.dto.LoginRequest;
import com.starwars.auth.dto.RegisterRequest;
import com.starwars.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes public endpoints for user registration and authentication.
 *
 * <p>These endpoints do NOT require a JWT token.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login to obtain a JWT token")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account and returns a JWT token.
     *
     * @param request registration details
     * @return {@code 201 Created} with a JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
               description = "Creates a new user account. Returns a JWT token that can be used immediately.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request login credentials
     * @return {@code 200 OK} with a JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login with existing credentials",
               description = "Authenticates the user and returns a JWT token. "
                             + "Include this token as 'Authorization: Bearer {token}' in all protected requests.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}