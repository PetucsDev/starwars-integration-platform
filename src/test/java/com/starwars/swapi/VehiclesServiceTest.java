package com.starwars.swapi;

import com.starwars.exception.ResourceNotFoundException;
import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.VehicleDto;
import com.starwars.swapi.dto.swapi.*;
import com.starwars.swapi.service.VehiclesService;
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
 * Unit tests for {@link VehiclesService}.
 *
 * <p>{@link SwapiClient} is mocked so no HTTP calls are made.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehiclesService")
class VehiclesServiceTest {

    @Mock
    private SwapiClient swapiClient;

    private VehiclesService vehiclesService;

    @BeforeEach
    void setUp() {
        vehiclesService = new VehiclesService(swapiClient);
    }

    @Test
    @DisplayName("listVehicles without name calls SWAPI paginated endpoint and returns summaries")
    void listVehicles_withoutName_returnsPaginatedSummaries() {
        SwapiPagedResponse swapiResponse = buildPagedResponse();
        when(swapiClient.get(contains("/api/vehicles?page=1&limit=10"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(swapiResponse);

        PagedResponse<VehicleDto> result = vehiclesService.listVehicles(1, 10, null);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).uid()).isEqualTo("4");
        assertThat(result.items().get(0).name()).isEqualTo("Sand Crawler");
        assertThat(result.totalRecords()).isEqualTo(39L);
        assertThat(result.totalPages()).isEqualTo(4);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("listVehicles with name calls SWAPI search endpoint and returns full DTOs")
    void listVehicles_withName_callsSearchAndReturnsFullDtos() {
        SwapiListResponse<VehicleProperties> searchResponse = buildSearchResponse();
        when(swapiClient.get(contains("/api/vehicles/?name="),
                any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<VehicleDto> result = vehiclesService.listVehicles(1, 10, "speeder");

        assertThat(result.items()).hasSize(1);
        VehicleDto vehicle = result.items().get(0);
        assertThat(vehicle.name()).isEqualTo("Snowspeeder");
        assertThat(vehicle.model()).isEqualTo("T-47 airspeeder");
        assertThat(vehicle.vehicleClass()).isEqualTo("airspeeder");
    }

    @Test
    @DisplayName("getById returns a fully populated VehicleDto")
    void getById_returnsFullVehicleDto() {
        SwapiSingleResponse<VehicleProperties> singleResponse = buildSingleResponse();
        when(swapiClient.get(contains("/api/vehicles/4"),
                any(ParameterizedTypeReference.class)))
                .thenReturn(singleResponse);

        VehicleDto result = vehiclesService.getById("4");

        assertThat(result.uid()).isEqualTo("4");
        assertThat(result.name()).isEqualTo("Sand Crawler");
        assertThat(result.vehicleClass()).isEqualTo("wheeled");
    }

    @Test
    @DisplayName("getById propagates ResourceNotFoundException from the client")
    void getById_propagatesNotFoundException() {
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThatThrownBy(() -> vehiclesService.getById("9999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listVehicles paginates search results correctly")
    void listVehicles_withName_paginatesResults() {
        SwapiListResponse<VehicleProperties> searchResponse = buildMultiResultSearchResponse(15);
        when(swapiClient.get(anyString(), any(ParameterizedTypeReference.class)))
                .thenReturn(searchResponse);

        PagedResponse<VehicleDto> page1 = vehiclesService.listVehicles(1, 10, "speeder");
        PagedResponse<VehicleDto> page2 = vehiclesService.listVehicles(2, 10, "speeder");

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
        response.setTotalRecords("39");
        response.setTotalPages(4);
        response.setNext("https://swapi.tech/api/vehicles?page=2&limit=10");
        response.setPrevious(null);
        response.setResults(List.of(
                new SwapiEntityRef("4", "Sand Crawler", "https://swapi.tech/api/vehicles/4"),
                new SwapiEntityRef("6", "T-16 skyhopper", "https://swapi.tech/api/vehicles/6")
        ));
        return response;
    }

    private SwapiListResponse<VehicleProperties> buildSearchResponse() {
        VehicleProperties props = buildVehicleProperties("Snowspeeder", "T-47 airspeeder", "airspeeder");
        SwapiFullEntity<VehicleProperties> entity = new SwapiFullEntity<>();
        entity.setUid("14");
        entity.setProperties(props);

        SwapiListResponse<VehicleProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(List.of(entity));
        return response;
    }

    private SwapiSingleResponse<VehicleProperties> buildSingleResponse() {
        VehicleProperties props = buildVehicleProperties("Sand Crawler", "Digger Crawler", "wheeled");
        SwapiFullEntity<VehicleProperties> entity = new SwapiFullEntity<>();
        entity.setUid("4");
        entity.setProperties(props);

        SwapiSingleResponse<VehicleProperties> response = new SwapiSingleResponse<>();
        response.setMessage("ok");
        response.setResult(entity);
        return response;
    }

    private SwapiListResponse<VehicleProperties> buildMultiResultSearchResponse(int count) {
        SwapiListResponse<VehicleProperties> response = new SwapiListResponse<>();
        response.setMessage("ok");
        response.setResult(
                java.util.stream.IntStream.rangeClosed(1, count)
                        .mapToObj(i -> {
                            VehicleProperties p = buildVehicleProperties(
                                    "Speeder " + i, "Model " + i, "airspeeder");
                            SwapiFullEntity<VehicleProperties> e = new SwapiFullEntity<>();
                            e.setUid(String.valueOf(i));
                            e.setProperties(p);
                            return e;
                        })
                        .toList()
        );
        return response;
    }

    private VehicleProperties buildVehicleProperties(String name, String model, String vehicleClass) {
        VehicleProperties props = new VehicleProperties();
        props.setName(name);
        props.setModel(model);
        props.setVehicleClass(vehicleClass);
        props.setManufacturer("Corellia Mining Corporation");
        props.setCostInCredits("150000");
        props.setLength("36.8");
        props.setCrew("46");
        props.setPassengers("30");
        props.setMaxAtmospheringSpeed("30");
        props.setCargoCapacity("50000");
        props.setConsumables("2 months");
        props.setFilms(List.of());
        props.setPilots(List.of());
        props.setUrl("https://swapi.tech/api/vehicles/4");
        return props;
    }
}
