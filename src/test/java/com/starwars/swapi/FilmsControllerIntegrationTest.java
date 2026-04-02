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
 * Integration tests for the Films API endpoints.
 *
 * <p>Uses WireMock to stub swapi.tech responses. Performs full auth
 * (register → login → use token) before calling protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Films Controller Integration Tests")
class FilmsControllerIntegrationTest {

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(9091));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.swapi.base-url", () -> "http://localhost:9091");
    }

    @BeforeEach
    void obtainToken() throws Exception {
        wireMockServer.resetAll();

        RegisterRequest reg = new RegisterRequest("filmsTester", "test1234", "films@test.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest("filmsTester", "test1234");
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
    @DisplayName("GET /api/films without Authorization returns 403")
    void listFilms_withoutToken_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/films"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/films with valid token returns 200 and paginated response")
    void listFilms_withValidToken_returnsPaginatedResponse() throws Exception {
        wireMockServer.stubFor(
                get(urlEqualTo("/api/films"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(FILMS_LIST_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/films")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].uid").value("1"))
                .andExpect(jsonPath("$.items[0].title").value("A New Hope"))
                .andExpect(jsonPath("$.totalRecords").value(3))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("GET /api/films?title=hope returns only matching films")
    void listFilms_withTitleFilter_returnsMatchingFilms() throws Exception {
        wireMockServer.stubFor(
                get(urlEqualTo("/api/films"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(FILMS_LIST_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/films")
                        .param("title", "hope")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("A New Hope"));
    }

    @Test
    @DisplayName("GET /api/films/{id} with valid token returns full film details")
    void getFilmById_withValidToken_returnsFilmDetails() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/films/1"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(FILM_DETAIL_RESPONSE))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/films/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.title").value("A New Hope"))
                .andExpect(jsonPath("$.director").value("George Lucas"))
                .andExpect(jsonPath("$.episodeId").value(4));
    }

    @Test
    @DisplayName("GET /api/films/{id} for non-existent resource returns 404")
    void getFilmById_notFound_returns404() throws Exception {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/films/9999"))
                        .willReturn(aResponse().withStatus(404))
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/api/films/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // =========================================================
    // SWAPI stub JSON payloads
    // =========================================================

    private static final String FILMS_LIST_RESPONSE = """
            {
              "message": "ok",
              "result": [
                {
                  "properties": {
                    "title": "A New Hope",
                    "episode_id": 4,
                    "director": "George Lucas",
                    "producer": "Gary Kurtz",
                    "release_date": "1977-05-25",
                    "opening_crawl": "It is a period of civil war...",
                    "characters": [],
                    "planets": [],
                    "starships": [],
                    "vehicles": [],
                    "species": [],
                    "url": "https://www.swapi.tech/api/films/1"
                  },
                  "uid": "1"
                },
                {
                  "properties": {
                    "title": "The Empire Strikes Back",
                    "episode_id": 5,
                    "director": "Irvin Kershner",
                    "producer": "Gary Kurtz",
                    "release_date": "1980-05-17",
                    "opening_crawl": "It is a dark time...",
                    "characters": [],
                    "planets": [],
                    "starships": [],
                    "vehicles": [],
                    "species": [],
                    "url": "https://www.swapi.tech/api/films/2"
                  },
                  "uid": "2"
                },
                {
                  "properties": {
                    "title": "Return of the Jedi",
                    "episode_id": 6,
                    "director": "Richard Marquand",
                    "producer": "Howard Kazanjian",
                    "release_date": "1983-05-25",
                    "opening_crawl": "Luke Skywalker has returned...",
                    "characters": [],
                    "planets": [],
                    "starships": [],
                    "vehicles": [],
                    "species": [],
                    "url": "https://www.swapi.tech/api/films/3"
                  },
                  "uid": "3"
                }
              ]
            }
            """;

    private static final String FILM_DETAIL_RESPONSE = """
            {
              "message": "ok",
              "result": {
                "properties": {
                  "title": "A New Hope",
                  "episode_id": 4,
                  "director": "George Lucas",
                  "producer": "Gary Kurtz",
                  "release_date": "1977-05-25",
                  "opening_crawl": "It is a period of civil war...",
                  "characters": ["https://www.swapi.tech/api/people/1"],
                  "planets": ["https://www.swapi.tech/api/planets/1"],
                  "starships": ["https://www.swapi.tech/api/starships/2"],
                  "vehicles": ["https://www.swapi.tech/api/vehicles/4"],
                  "species": ["https://www.swapi.tech/api/species/1"],
                  "url": "https://www.swapi.tech/api/films/1"
                },
                "uid": "1"
              }
            }
            """;
}
