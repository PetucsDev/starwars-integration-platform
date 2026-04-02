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
 * Integration tests for the Starships API endpoints.
 *
 * <p>Uses WireMock to stub swapi.tech responses. Performs full auth
 * (register → login → use token) before calling protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Starships Controller Integration Tests")
class StarshipsControllerIntegrationTest {

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(9092));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.swapi.base-url", () -> "http://localhost:9092");
    }

    @BeforeEach
    void obtainToken() throws Exception {
        wireMockServer.resetAll();

        RegisterRequest reg = new RegisterRequest("starshipsTester", "test1234", "starships@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest("starshipsTester", "test1234");
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
    @DisplayName("GET /api/starships without Authorization returns 403")
    void listStarships_withoutToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/starships"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/starships with valid token returns 200 and paginated response")
    void listStarships_withValidToken_returnsPaginatedResponse() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/starships"))
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("limit", equalTo("10"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(STARSHIPS_LIST_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/starships")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].uid").value("9"))
                .andExpect(jsonPath("$.items[0].name").value("Death Star"))
                .andExpect(jsonPath("$.totalRecords").value(36))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("GET /api/starships/{id} with valid token returns full starship details")
    void getStarshipById_withValidToken_returnsStarshipDetails() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/starships/10"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(STARSHIP_DETAIL_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/starships/10")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("10"))
                .andExpect(jsonPath("$.name").value("Millennium Falcon"))
                .andExpect(jsonPath("$.model").value("YT-1300 light freighter"))
                .andExpect(jsonPath("$.hyperdriveRating").value("0.5"));
    }

    @Test
    @DisplayName("GET /api/starships/{id} for non-existent resource returns 404")
    void getStarshipById_notFound_returns404() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/starships/9999"))
                        .willReturn(aResponse().withStatus(404))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/starships/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/starships?name=falcon calls SWAPI search and returns full details")
    void listStarships_withNameFilter_callsSearchEndpoint() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/starships/"))
                        .withQueryParam("name", equalTo("falcon"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(STARSHIPS_SEARCH_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/starships")
                        .param("name", "falcon")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Millennium Falcon"))
                .andExpect(jsonPath("$.items[0].hyperdriveRating").value("0.5"));
    }

    // =========================================================
    // SWAPI stub JSON payloads
    // =========================================================

    private static final String STARSHIPS_LIST_RESPONSE = """
            {
              "message": "ok",
              "total_records": "36",
              "total_pages": 4,
              "previous": null,
              "next": "https://www.swapi.tech/api/starships?page=2&limit=10",
              "results": [
                {"uid": "9",  "name": "Death Star",        "url": "https://www.swapi.tech/api/starships/9"},
                {"uid": "10", "name": "Millennium Falcon", "url": "https://www.swapi.tech/api/starships/10"}
              ]
            }
            """;

    private static final String STARSHIP_DETAIL_RESPONSE = """
            {
              "message": "ok",
              "result": {
                "properties": {
                  "name": "Millennium Falcon",
                  "model": "YT-1300 light freighter",
                  "starship_class": "Light freighter",
                  "manufacturer": "Corellian Engineering Corporation",
                  "cost_in_credits": "100000",
                  "length": "34.75",
                  "crew": "4",
                  "passengers": "6",
                  "max_atmosphering_speed": "1050",
                  "hyperdrive_rating": "0.5",
                  "MGLT": "75",
                  "cargo_capacity": "100000",
                  "consumables": "2 months",
                  "films": [],
                  "pilots": [],
                  "url": "https://www.swapi.tech/api/starships/10"
                },
                "uid": "10"
              }
            }
            """;

    private static final String STARSHIPS_SEARCH_RESPONSE = """
            {
              "message": "ok",
              "result": [
                {
                  "properties": {
                    "name": "Millennium Falcon",
                    "model": "YT-1300 light freighter",
                    "starship_class": "Light freighter",
                    "manufacturer": "Corellian Engineering Corporation",
                    "cost_in_credits": "100000",
                    "length": "34.75",
                    "crew": "4",
                    "passengers": "6",
                    "max_atmosphering_speed": "1050",
                    "hyperdrive_rating": "0.5",
                    "MGLT": "75",
                    "cargo_capacity": "100000",
                    "consumables": "2 months",
                    "films": [],
                    "pilots": [],
                    "url": "https://www.swapi.tech/api/starships/10"
                  },
                  "uid": "10"
                }
              ]
            }
            """;
}
