package com.starwars.swapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.starwars.auth.dto.LoginRequest;
import com.starwars.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the People API endpoints.
 *
 * <p>Uses WireMock to stub swapi.tech responses, and performs full auth
 * (register → login → use token) before calling protected endpoints.
 *
 * <p>Note: MockMvc request builder methods are accessed via {@link MockMvcRequestBuilders}
 * explicitly to avoid static import conflicts with WireMock's identically-named methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("People Controller Integration Tests")
class PeopleControllerIntegrationTest {

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(9090));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.swapi.base-url", () -> "http://localhost:9090");
    }

    @BeforeEach
    void obtainToken() throws Exception {
        wireMockServer.resetAll();

        // Register (ignore 409 if already exists)
        RegisterRequest reg = new RegisterRequest("peopleTester", "test1234", "people@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Login and extract token
        LoginRequest login = new LoginRequest("peopleTester", "test1234");
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body).get("token").asText();
    }

    @AfterEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("GET /api/people without Authorization returns 403")
    void listPeople_withoutToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/people"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/people with valid token returns 200 and paginated response")
    void listPeople_withValidToken_returnsPaginatedResponse() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/people"))
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("limit", equalTo("10"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(PEOPLE_LIST_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].uid").value("1"))
                .andExpect(jsonPath("$.items[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.totalRecords").value(82))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("GET /api/people/{id} with valid token returns full person details")
    void getPersonById_withValidToken_returnsPersonDetails() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/people/1"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(PERSON_DETAIL_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/people/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.height").value("172"))
                .andExpect(jsonPath("$.gender").value("male"));
    }

    @Test
    @DisplayName("GET /api/people/{id} for non-existent resource returns 404")
    void getPersonById_notFound_returns404() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/people/9999"))
                        .willReturn(aResponse().withStatus(404))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/people/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/people?name=luke calls SWAPI search and returns full details")
    void listPeople_withNameFilter_callsSearchEndpoint() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/people/"))
                        .withQueryParam("name", equalTo("luke"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(PEOPLE_SEARCH_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .param("name", "luke")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.items[0].height").value("172"));
    }

    @Test
    @DisplayName("GET /api/people?page=abc returns 400 with descriptive message")
    void listPeople_withNonNumericPage_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .param("page", "abc")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'page' must be a valid int"));
    }

    @Test
    @DisplayName("GET /api/people?page=0 returns 400 — page must be >= 1")
    void listPeople_withPageZero_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/people")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // =========================================================
    // SWAPI stub JSON payloads
    // =========================================================

    private static final String PEOPLE_LIST_RESPONSE = """
            {
              "message": "ok",
              "total_records": "82",
              "total_pages": 9,
              "previous": null,
              "next": "https://www.swapi.tech/api/people?page=2&limit=10",
              "results": [
                {"uid": "1", "name": "Luke Skywalker", "url": "https://www.swapi.tech/api/people/1"},
                {"uid": "2", "name": "C-3PO", "url": "https://www.swapi.tech/api/people/2"}
              ]
            }
            """;

    private static final String PERSON_DETAIL_RESPONSE = """
            {
              "message": "ok",
              "result": {
                "properties": {
                  "name": "Luke Skywalker",
                  "height": "172",
                  "mass": "77",
                  "hair_color": "blond",
                  "skin_color": "fair",
                  "eye_color": "blue",
                  "birth_year": "19BBY",
                  "gender": "male",
                  "homeworld": "https://www.swapi.tech/api/planets/1",
                  "url": "https://www.swapi.tech/api/people/1"
                },
                "description": "A person within the Star Wars universe",
                "uid": "1"
              }
            }
            """;

    private static final String PEOPLE_SEARCH_RESPONSE = """
            {
              "message": "ok",
              "result": [
                {
                  "properties": {
                    "name": "Luke Skywalker",
                    "height": "172",
                    "mass": "77",
                    "hair_color": "blond",
                    "skin_color": "fair",
                    "eye_color": "blue",
                    "birth_year": "19BBY",
                    "gender": "male",
                    "homeworld": "https://www.swapi.tech/api/planets/1",
                    "url": "https://www.swapi.tech/api/people/1"
                  },
                  "uid": "1"
                }
              ]
            }
            """;
}