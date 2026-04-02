package com.starwars.swapi;

import com.starwars.exception.ResourceNotFoundException;
import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.PersonDto;
import com.starwars.swapi.dto.swapi.*;
import com.starwars.swapi.service.PeopleService;
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
 * Unit tests for {@link PeopleService}.
 *
 * <p>{@link SwapiClient} is mocked so no HTTP calls are made.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PeopleService")
class PeopleServiceTest {

    @Mock
    private SwapiClient swapiClient;

    private PeopleService peopleService;

    @BeforeEach
    void setUp() {
        peopleService = new PeopleService(swapiClient);
    }

    @Test
    @DisplayName("listPeople without name calls SWAPI paginated endpoint and returns summaries")
    void listPeople_withoutName_returnsPaginatedSummaries() {
        SwapiPagedResponse swapiResponse = buildPagedResponse();
        when(swapiClient.get(contains("/api/people?page=1&limit=10"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(swapiResponse);

        PagedResponse<PersonDto> result = peopleService.listPeople(1, 10, null);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).uid()).isEqualTo("1");
        assertThat(result.items().get(0).name()).isEqualTo("Luke Skywalker");
        assertThat(result.totalRecords()).isEqualTo(82L);
        assertThat(result.totalPages()).isEqualTo(9);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("listPeople with name calls SWAPI search endpoint and returns full DTOs")
    void listPeople_withName_callsSearchAndReturnsFullDtos() {
        SwapiListResponse<PersonProperties> searchResponse = buildSearchResponse();
        when(swapiClient.get(contains("/api/people/?name=luke"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<PersonDto> result = peopleService.listPeople(1, 10, "luke");

        assertThat(result.items()).hasSize(1);
        PersonDto person = result.items().get(0);
        assertThat(person.name()).isEqualTo("Luke Skywalker");
        assertThat(person.height()).isEqualTo("172");
        assertThat(person.gender()).isEqualTo("male");
    }

    @Test
    @DisplayName("getById returns a fully populated PersonDto")
    void getById_returnsFullPersonDto() {
        SwapiSingleResponse<PersonProperties> singleResponse = buildSingleResponse();
        when(swapiClient.get(contains("/api/people/1"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(singleResponse);

        PersonDto result = peopleService.getById("1");

        assertThat(result.uid()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Luke Skywalker");
        assertThat(result.birthYear()).isEqualTo("19BBY");
    }

    @Test
    @DisplayName("getById propagates ResourceNotFoundException from the client")
    void getById_propagatesNotFoundException() {
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThatThrownBy(() -> peopleService.getById("9999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listPeople paginates search results correctly")
    void listPeople_withName_paginatesResults() {
        SwapiListResponse<PersonProperties> searchResponse = buildMultiResultSearchResponse(15);
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<PersonDto> page1 = peopleService.listPeople(1, 10, "sky");
        PagedResponse<PersonDto> page2 = peopleService.listPeople(2, 10, "sky");

        assertThat(page1.items()).hasSize(10);
        assertThat(page2.items()).hasSize(5);
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
        response.setTotalRecords("82");
        response.setTotalPages(9);
        response.setNext("https://swapi.tech/api/people?page=2&limit=10");
        response.setPrevious(null);
        response.setResults(List.of(
                new SwapiEntityRef("1", "Luke Skywalker", "https://swapi.tech/api/people/1"),
                new SwapiEntityRef("2", "C-3PO", "https://swapi.tech/api/people/2")
        ));
        return response;
    }

    private SwapiListResponse<PersonProperties> buildSearchResponse() {
        PersonProperties props = buildPersonProperties("Luke Skywalker", "172", "male", "19BBY");
        SwapiFullEntity<PersonProperties> entity = new SwapiFullEntity<>();
        entity.setUid("1");
        entity.setProperties(props);

        SwapiListResponse<PersonProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(List.of(entity));
        return response;
    }

    private SwapiSingleResponse<PersonProperties> buildSingleResponse() {
        PersonProperties props = buildPersonProperties("Luke Skywalker", "172", "male", "19BBY");
        SwapiFullEntity<PersonProperties> entity = new SwapiFullEntity<>();
        entity.setUid("1");
        entity.setProperties(props);

        SwapiSingleResponse<PersonProperties> response = new SwapiSingleResponse<>();
        response.setMessage("ok");
        response.setResult(entity);
        return response;
    }

    private SwapiListResponse<PersonProperties> buildMultiResultSearchResponse(int count) {
        SwapiListResponse<PersonProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(
                java.util.stream.IntStream.rangeClosed(1, count)
                        .mapToObj(i -> {
                            PersonProperties p = buildPersonProperties("Person " + i, "170", "male", "20BBY");
                            SwapiFullEntity<PersonProperties> e = new SwapiFullEntity<>();
                            e.setUid(String.valueOf(i));
                            e.setProperties(p);
                            return e;
                        })
                        .toList()
        );
        return response;
    }

    private PersonProperties buildPersonProperties(String name, String height,
                                                    String gender, String birthYear) {
        PersonProperties props = new PersonProperties();
        props.setName(name);
        props.setHeight(height);
        props.setMass("77");
        props.setHairColor("blond");
        props.setSkinColor("fair");
        props.setEyeColor("blue");
        props.setBirthYear(birthYear);
        props.setGender(gender);
        props.setHomeworld("https://swapi.tech/api/planets/1");
        props.setUrl("https://swapi.tech/api/people/1");
        return props;
    }
}