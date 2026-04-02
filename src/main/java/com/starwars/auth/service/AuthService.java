package com.starwars.auth.service;

import com.starwars.auth.dto.AuthResponse;
import com.starwars.auth.dto.LoginRequest;
import com.starwars.auth.dto.RegisterRequest;
import com.starwars.auth.entity.AppUser;
import com.starwars.auth.repository.UserRepository;
import com.starwars.exception.ConflictException;
import com.starwars.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login business logic.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param request registration details
     * @return {@link AuthResponse} with a signed JWT
     * @throws ConflictException if the username or email is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email '" + request.email() + "' is already registered");
        }

        AppUser user = new AppUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email()
        );
        userRepository.save(user);

        return buildAuthResponse(request.username(), request.password());
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request login credentials
     * @return {@link AuthResponse} with a signed JWT
     */
    public AuthResponse login(LoginRequest request) {
        return buildAuthResponse(request.username(), request.password());
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private AuthResponse buildAuthResponse(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        return new AuthResponse(token, jwtTokenProvider.extractExpiration(token), userDetails.getUsername());
    }
}