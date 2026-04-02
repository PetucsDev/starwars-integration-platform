package com.starwars.swapi;

import com.starwars.exception.ResourceNotFoundException;
import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.StarshipDto;
import com.starwars.swapi.dto.swapi.*;
import com.starwars.swapi.service.StarshipsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StarshipsService}.
 *
 * <p>{@link SwapiClient} is mocked so no HTTP calls are made.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StarshipsService")
class StarshipsServiceTest {

    @Mock
    private SwapiClient swapiClient;

    private StarshipsService starshipsService;

    @BeforeEach
    void setUp() {
        starshipsService = new StarshipsService(swapiClient);
    }

    @Test
    @DisplayName("listStarships without name calls SWAPI paginated endpoint and returns summaries")
    void listStarships_withoutName_returnsPaginatedSummaries() {
        SwapiPagedResponse swapiResponse = buildPagedResponse();
        when(swapiClient.get(contains("/api/starships?page=1&limit=10"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(swapiResponse);

        PagedResponse<StarshipDto> result = starshipsService.listStarships(1, 10, null);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).uid()).isEqualTo("9");
        assertThat(result.items().get(0).name()).isEqualTo("Death Star");
        assertThat(result.totalRecords()).isEqualTo(36L);
        assertThat(result.totalPages()).isEqualTo(4);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("listStarships with name calls SWAPI search endpoint and returns full DTOs")
    void listStarships_withName_callsSearchAndReturnsFullDtos() {
        SwapiListResponse<StarshipProperties> searchResponse = buildSearchResponse();
        when(swapiClient.get(contains("/api/starships/?name="),
                any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<StarshipDto> result = starshipsService.listStarships(1, 10, "falcon");

        assertThat(result.items()).hasSize(1);
        StarshipDto starship = result.items().get(0);
        assertThat(starship.name()).isEqualTo("Millennium Falcon");
        assertThat(starship.model()).isEqualTo("YT-1300 light freighter");
        assertThat(starship.hyperdriveRating()).isEqualTo("0.5");
    }

    @Test
    @DisplayName("getById returns a fully populated StarshipDto")
    void getById_returnsFullStarshipDto() {
        SwapiSingleResponse<StarshipProperties> singleResponse = buildSingleResponse();
        when(swapiClient.get(contains("/api/starships/10"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(singleResponse);

        StarshipDto result = starshipsService.getById("10");

        assertThat(result.uid()).isEqualTo("10");
        assertThat(result.name()).isEqualTo("Millennium Falcon");
        assertThat(result.starshipClass()).isEqualTo("Light freighter");
    }

    @Test
    @DisplayName("getById propagates ResourceNotFoundException from the client")
    void getById_propagatesNotFoundException() {
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThatThrownBy(() -> starshipsService.getById("9999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listStarships paginates search results correctly")
    void listStarships_withName_paginatesResults() {
        SwapiListResponse<StarshipProperties> searchResponse = buildMultiResultSearchResponse(12);
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<StarshipDto> page1 = starshipsService.listStarships(1, 10, "star");
        PagedResponse<StarshipDto> page2 = starshipsService.listStarships(2, 10, "star");

        assertThat(page1.items()).hasSize(10);
        assertThat(page2.items()).hasSize(2);
        assertThat(page1.totalPages()).isEqualTo(2);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page2.hasPrevious()).isTrue();
    }

    // =========================================================
    // Test data builders
    // =========================================================

    private SwapiPagedResponse buildPagedResponse() {
        SwapiPagedResponse response = new SwapiPagedResponse();
        response.setMessage("ok");
        response.setTotalRecords("36");
        response.setTotalPages(4);
        response.setNext("https://swapi.tech/api/starships?page=2&limit=10");
        response.setPrevious(null);
        response.setResults(List.of(
                new SwapiEntityRef("9", "Death Star", "https://swapi.tech/api/starships/9"),
                new SwapiEntityRef("10", "Millennium Falcon", "https://swapi.tech/api/starships/10")
        ));
        return response;
    }

    private SwapiListResponse<StarshipProperties> buildSearchResponse() {
        StarshipProperties props = buildStarshipProperties(
                "Millennium Falcon", "YT-1300 light freighter", "Light freighter", "0.5");
        SwapiFullEntity<StarshipProperties> entity = new SwapiFullEntity<>();
        entity.setUid("10");
        entity.setProperties(props);

        SwapiListResponse<StarshipProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(List.of(entity));
        return response;
    }

    private SwapiSingleResponse<StarshipProperties> buildSingleResponse() {
        StarshipProperties props = buildStarshipProperties(
                "Millennium Falcon", "YT-1300 light freighter", "Light freighter", "0.5");
        SwapiFullEntity<StarshipProperties> entity = new SwapiFullEntity<>();
        entity.setUid("10");
        entity.setProperties(props);

        SwapiSingleResponse<StarshipProperties> response = new SwapiSingleResponse<>();
        response.setMessage("ok");
        response.setResult(entity);
        return response;
    }

    private SwapiListResponse<StarshipProperties> buildMultiResultSearchResponse(int count) {
        SwapiListResponse<StarshipProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(
                java.util.stream.IntStream.rangeClosed(1, count)
                        .mapToObj(i -> {
                            StarshipProperties p = buildStarshipProperties(
                                    "Starship " + i, "Model " + i, "Cruiser", "2.0");
                            SwapiFullEntity<StarshipProperties> e = new SwapiFullEntity<>();
                            e.setUid(String.valueOf(i));
                            e.setProperties(p);
                            return e;
                        })
                        .toList()
        );
        return response;
    }

    private StarshipProperties buildStarshipProperties(String name, String model,
                                                        String starshipClass, String hyperdriveRating) {
        StarshipProperties props = new StarshipProperties();
        props.setName(name);
        props.setModel(model);
        props.setStarshipClass(starshipClass);
        props.setManufacturer("Corellian Engineering Corporation");
        props.setCostInCredits("100000");
        props.setLength("34.75");
        props.setCrew("4");
        props.setPassengers("6");
        props.setMaxAtmospheringSpeed("1050");
        props.setHyperdriveRating(hyperdriveRating);
        props.setMglt("75");
        props.setCargoCapacity("100000");
        props.setConsumables("2 months");
        props.setFilms(List.of());
        props.setPilots(List.of());
        props.setUrl("https://swapi.tech/api/starships/10");
        return props;
    }
}
