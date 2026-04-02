package com.starwars.swapi.service;

import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.VehicleDto;
import com.starwars.swapi.dto.swapi.*;
import com.starwars.swapi.util.PaginationUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service that fetches and maps Vehicles data from the Star Wars API.
 */
@Service
public class VehiclesService {

    private final SwapiClient swapiClient;

    public VehiclesService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    /**
     * Lists vehicles with optional name filtering.
     *
     * @param page  page number (1-based)
     * @param limit items per page
     * @param name  optional name filter
     * @return paginated list of vehicles
     */
    @Cacheable(value = "vehicles", key = "#page + '-' + #limit + '-' + #name")
    public PagedResponse<VehicleDto> listVehicles(int page, int limit, String name) {
        if (StringUtils.hasText(name)) {
            return searchByName(name, page, limit);
        }
        return fetchPage(page, limit);
    }

    /**
     * Retrieves a single vehicle by its SWAPI ID.
     *
     * @param id the SWAPI vehicle ID
     * @return full vehicle details
     */
    @Cacheable(value = "vehicles", key = "'id-' + #id")
    public VehicleDto getById(String id) {
        SwapiSingleResponse<VehicleProperties> response = swapiClient.get(
                "/api/vehicles/" + id,
                new ParameterizedTypeReference<>() {}
        );
        SwapiFullEntity<VehicleProperties> entity = response.getResult();
        return mapToDto(entity.getUid(), entity.getProperties());
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private PagedResponse<VehicleDto> fetchPage(int page, int limit) {
        SwapiPagedResponse response = swapiClient.get(
                "/api/vehicles?page=" + page + "&limit=" + limit,
                new ParameterizedTypeReference<>() {}
        );

        List<VehicleDto> items = response.getResults().stream()
                .map(ref -> VehicleDto.summary(ref.uid(), ref.name(), ref.url()))
                .toList();

        long totalRecords = PaginationUtils.parseLong(response.getTotalRecords());
        return PagedResponse.of(
                items, page, limit,
                response.getTotalPages(), totalRecords,
                response.getNext() != null,
                response.getPrevious() != null
        );
    }

    private PagedResponse<VehicleDto> searchByName(String name, int page, int limit) {
        SwapiListResponse<VehicleProperties> response = swapiClient.get(
                "/api/vehicles/?name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8),
                new ParameterizedTypeReference<>() {}
        );

        List<VehicleDto> allMatches = response.getResult().stream()
                .map(e -> mapToDto(e.getUid(), e.getProperties()))
                .toList();

        return PaginationUtils.paginate(allMatches, page, limit);
    }

    private VehicleDto mapToDto(String uid, VehicleProperties p) {
        return new VehicleDto(
                uid,
                p.getName(),
                p.getModel(),
                p.getVehicleClass(),
                p.getManufacturer(),
                p.getCostInCredits(),
                p.getLength(),
                p.getCrew(),
                p.getPassengers(),
                p.getMaxAtmospheringSpeed(),
                p.getCargoCapacity(),
                p.getConsumables(),
                p.getFilms(),
                p.getPilots(),
                p.getUrl()
        );
    }

}