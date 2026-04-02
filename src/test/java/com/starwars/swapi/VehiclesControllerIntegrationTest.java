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
 * Integration tests for the Vehicles API endpoints.
 *
 * <p>Uses WireMock to stub swapi.tech responses. Performs full auth
 * (register → login → use token) before calling protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Vehicles Controller Integration Tests")
class VehiclesControllerIntegrationTest {

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(9093));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.swapi.base-url", () -> "http://localhost:9093");
    }

    @BeforeEach
    void obtainToken() throws Exception {
        wireMockServer.resetAll();

        RegisterRequest reg = new RegisterRequest("vehiclesTester", "test1234", "vehicles@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest("vehiclesTester", "test1234");
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
    @DisplayName("GET /api/vehicles without Authorization returns 403")
    void listVehicles_withoutToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/vehicles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/vehicles with valid token returns 200 and paginated response")
    void listVehicles_withValidToken_returnsPaginatedResponse() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/vehicles"))
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("limit", equalTo("10"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(VEHICLES_LIST_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/vehicles")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].uid").value("4"))
                .andExpect(jsonPath("$.items[0].name").value("Sand Crawler"))
                .andExpect(jsonPath("$.totalRecords").value(39))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} with valid token returns full vehicle details")
    void getVehicleById_withValidToken_returnsVehicleDetails() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/vehicles/4"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(VEHICLE_DETAIL_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/vehicles/4")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("4"))
                .andExpect(jsonPath("$.name").value("Sand Crawler"))
                .andExpect(jsonPath("$.model").value("Digger Crawler"))
                .andExpect(jsonPath("$.vehicleClass").value("wheeled"));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} for non-existent resource returns 404")
    void getVehicleById_notFound_returns404() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/vehicles/9999"))
                        .willReturn(aResponse().withStatus(404))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/vehicles/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/vehicles?name=speeder calls SWAPI search and returns full details")
    void listVehicles_withNameFilter_callsSearchEndpoint() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/vehicles/"))
                        .withQueryParam("name", equalTo("speeder"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(VEHICLES_SEARCH_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/vehicles")
                        .param("name", "speeder")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Snowspeeder"))
                .andExpect(jsonPath("$.items[0].vehicleClass").value("airspeeder"));
    }

    // =========================================================
    // SWAPI stub JSON payloads
    // =========================================================

    private static final String VEHICLES_LIST_RESPONSE = """
            {
              "message": "ok",
              "total_records": "39",
              "total_pages": 4,
              "previous": null,
              "next": "https://www.swapi.tech/api/vehicles?page=2&limit=10",
              "results": [
                {"uid": "4", "name": "Sand Crawler",   "url": "https://www.swapi.tech/api/vehicles/4"},
                {"uid": "6", "name": "T-16 skyhopper", "url": "https://www.swapi.tech/api/vehicles/6"}
              ]
            }
            """;

    private static final String VEHICLE_DETAIL_RESPONSE = """
            {
              "message": "ok",
              "result": {
                "properties": {
                  "name": "Sand Crawler",
                  "model": "Digger Crawler",
                  "vehicle_class": "wheeled",
                  "manufacturer": "Corellia Mining Corporation",
                  "cost_in_credits": "150000",
                  "length": "36.8",
                  "crew": "46",
                  "passengers": "30",
                  "max_atmosphering_speed": "30",
                  "cargo_capacity": "50000",
                  "consumables": "2 months",
                  "films": [],
                  "pilots": [],
                  "url": "https://www.swapi.tech/api/vehicles/4"
                },
                "uid": "4"
              }
            }
            """;

    private static final String VEHICLES_SEARCH_RESPONSE = """
            {
              "message": "ok",
              "result": [
                {
                  "properties": {
                    "name": "Snowspeeder",
                    "model": "T-47 airspeeder",
                    "vehicle_class": "airspeeder",
                    "manufacturer": "Incom corporation",
                    "cost_in_credits": "unknown",
                    "length": "4.5",
                    "crew": "2",
                    "passengers": "0",
                    "max_atmosphering_speed": "650",
                    "cargo_capacity": "10",
                    "consumables": "none",
                    "films": [],
                    "pilots": [],
                    "url": "https://www.swapi.tech/api/vehicles/14"
                  },
                  "uid": "14"
                }
              ]
            }
            """;
}
