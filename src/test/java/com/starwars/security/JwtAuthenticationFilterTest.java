package com.starwars.security;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying the JWT authentication filter behaviour
 * across the full Spring Security filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("JwtAuthenticationFilter Security Tests")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Protected endpoint without token returns 403")
    void protectedEndpoint_withoutToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/people"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint with malformed token returns 403")
    void protectedEndpoint_withMalformedToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .header("Authorization", "Bearer this.is.not.a.valid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint with valid token is accepted")
    void protectedEndpoint_withValidToken_isAccepted() throws Exception {
        // Register user
        RegisterRequest reg = new RegisterRequest("filterUser", "password123", "filter@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Login
        LoginRequest login = new LoginRequest("filterUser", "password123");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Access protected endpoint — WireMock is not running in this test class,
        // so the SWAPI call will fail with SwapiException → 502.
        // What matters is that the request passed authentication (not 401/403).
        int status = mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();

        // The request was authenticated — it should not be 401 or 403
        assert status != 401 && status != 403
                : "Expected authenticated request but got HTTP " + status;
    }

    @Test
    @DisplayName("Public endpoints are accessible without a token")
    void publicEndpoints_areAccessibleWithoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // redirects to swagger-ui/index.html
    }
}