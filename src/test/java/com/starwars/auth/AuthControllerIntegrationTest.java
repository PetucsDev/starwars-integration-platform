package com.starwars.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.auth.dto.LoginRequest;
import com.starwars.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the authentication flow.
 *
 * <p>Starts the full Spring context with an H2 in-memory database.
 * Each test class uses a fresh application context ({@code @DirtiesContext})
 * to ensure no state leaks between tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // Register tests
    // =========================================================

    @Test
    @DisplayName("POST /api/auth/register with valid data returns 201 and a token")
    void register_withValidData_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest("jedimaster", "force123", "jedi@galaxy.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("jedimaster"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate username returns 409")
    void register_withDuplicateUsername_returns409() throws Exception {
        RegisterRequest first = new RegisterRequest("siths", "dark123", "sith1@empire.com");
        RegisterRequest duplicate = new RegisterRequest("siths", "dark456", "sith2@empire.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid email returns 400")
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "pass123", "not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/register with short password returns 400")
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("user2", "abc", "user@test.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // Login tests
    // =========================================================

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 and a token")
    void login_withValidCredentials_returns200AndToken() throws Exception {
        // First register
        RegisterRequest registerReq = new RegisterRequest("rebel1", "rebel123", "rebel@alliance.org");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest loginReq = new LoginRequest("rebel1", "rebel123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("rebel1"));
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong password returns 401")
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("rebel2", "right123", "rebel2@alliance.org");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("rebel2", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login with non-existent user returns 401")
    void login_withNonExistentUser_returns401() throws Exception {
        LoginRequest loginReq = new LoginRequest("nobody", "pass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}