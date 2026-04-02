package com.starwars.swapi;

import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.FilmDto;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.swapi.*;
import com.starwars.swapi.service.FilmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FilmsService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FilmsService")
class FilmsServiceTest {

    @Mock
    private SwapiClient swapiClient;

    private FilmsService filmsService;

    @BeforeEach
    void setUp() {
        filmsService = new FilmsService(swapiClient);
    }

    @Test
    @DisplayName("listFilms without title filter returns all films paginated")
    void listFilms_withoutFilter_returnsAllFilms() {
        when(swapiClient.get(contains("/api/films"), any(ParameterizedTypeReference.class)))
                .thenReturn(buildAllFilmsResponse());

        PagedResponse<FilmDto> result = filmsService.listFilms(1, 6, null);

        assertThat(result.items()).hasSize(3);
        assertThat(result.totalRecords()).isEqualTo(3L);
        assertThat(result.items().get(0).title()).isEqualTo("A New Hope");
    }

    @Test
    @DisplayName("listFilms with title filter returns only matching films")
    void listFilms_withTitleFilter_returnsMatchingFilms() {
        when(swapiClient.get(contains("/api/films"), any(ParameterizedTypeReference.class)))
                .thenReturn(buildAllFilmsResponse());

        PagedResponse<FilmDto> result = filmsService.listFilms(1, 6, "empire");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("The Empire Strikes Back");
    }

    @Test
    @DisplayName("listFilms with title filter is case-insensitive")
    void listFilms_titleFilterIsCaseInsensitive() {
        when(swapiClient.get(contains("/api/films"), any(ParameterizedTypeReference.class)))
                .thenReturn(buildAllFilmsResponse());

        PagedResponse<FilmDto> result = filmsService.listFilms(1, 6, "HOPE");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("A New Hope");
    }

    @Test
    @DisplayName("getById returns a fully populated FilmDto")
    void getById_returnsFullFilmDto() {
        SwapiSingleResponse<FilmProperties> response = buildSingleFilmResponse();
        when(swapiClient.get(contains("/api/films/1"), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        FilmDto result = filmsService.getById("1");

        assertThat(result.uid()).isEqualTo("1");
        assertThat(result.title()).isEqualTo("A New Hope");
        assertThat(result.episodeId()).isEqualTo(4);
        assertThat(result.director()).isEqualTo("George Lucas");
        assertThat(result.openingCrawl()).isNotBlank();
    }

    // =========================================================
    // Test data builders
    // =========================================================

    private SwapiListResponse<FilmProperties> buildAllFilmsResponse() {
        SwapiListResponse<FilmProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(List.of(
                buildFilmEntity("1", "A New Hope", 4, "George Lucas", "1977-05-25"),
                buildFilmEntity("2", "The Empire Strikes Back", 5, "Irvin Kershner", "1980-05-17"),
                buildFilmEntity("3", "Return of the Jedi", 6, "Richard Marquand", "1983-05-25")
        ));
        return response;
    }

    private SwapiFullEntity<FilmProperties> buildFilmEntity(String uid, String title,
                                                              int episodeId, String director,
                                                              String releaseDate) {
        FilmProperties props = new FilmProperties();
        props.setTitle(title);
        props.setEpisodeId(episodeId);
        props.setDirector(director);
        props.setProducer("Gary Kurtz");
        props.setReleaseDate(releaseDate);
        props.setOpeningCrawl("It is a dark time for the galaxy...");
        props.setCharacters(List.of("https://swapi.tech/api/people/1"));
        props.setPlanets(List.of());
        props.setStarships(List.of());
        props.setVehicles(List.of());
        props.setSpecies(List.of());
        props.setUrl("https://swapi.tech/api/films/" + uid);

        SwapiFullEntity<FilmProperties> entity = new SwapiFullEntity<>();
        entity.setUid(uid);
        entity.setProperties(props);
        return entity;
    }

    private SwapiSingleResponse<FilmProperties> buildSingleFilmResponse() {
        SwapiSingleResponse<FilmProperties> response = new SwapiSingleResponse<>();
        response.setMessage("ok");
        response.setResult(buildFilmEntity("1", "A New Hope", 4, "George Lucas", "1977-05-25"));
        return response;
    }
}