package com.starwars.swapi.service;

import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.StarshipDto;
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
 * Service that fetches and maps Starships data from the Star Wars API.
 */
@Service
public class StarshipsService {

    private final SwapiClient swapiClient;

    public StarshipsService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    /**
     * Lists starships with optional name filtering.
     *
     * @param page  page number (1-based)
     * @param limit items per page
     * @param name  optional name filter
     * @return paginated list of starships
     */
    @Cacheable(value = "starships", key = "#page + '-' + #limit + '-' + #name")
    public PagedResponse<StarshipDto> listStarships(int page, int limit, String name) {
        if (StringUtils.hasText(name)) {
            return searchByName(name, page, limit);
        }
        return fetchPage(page, limit);
    }

    /**
     * Retrieves a single starship by its SWAPI ID.
     *
     * @param id the SWAPI starship ID
     * @return full starship details
     */
    @Cacheable(value = "starships", key = "'id-' + #id")
    public StarshipDto getById(String id) {
        SwapiSingleResponse<StarshipProperties> response = swapiClient.get(
                "/api/starships/" + id,
                new ParameterizedTypeReference<>() {}
        );
        SwapiFullEntity<StarshipProperties> entity = response.getResult();
        return mapToDto(entity.getUid(), entity.getProperties());
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private PagedResponse<StarshipDto> fetchPage(int page, int limit) {
        SwapiPagedResponse response = swapiClient.get(
                "/api/starships?page=" + page + "&limit=" + limit,
                new ParameterizedTypeReference<>() {}
        );

        List<StarshipDto> items = response.getResults().stream()
                .map(ref -> StarshipDto.summary(ref.uid(), ref.name(), ref.url()))
                .toList();

        long totalRecords = PaginationUtils.parseLong(response.getTotalRecords());
        return PagedResponse.of(
                items, page, limit,
                response.getTotalPages(), totalRecords,
                response.getNext() != null,
                response.getPrevious() != null
        );
    }

    private PagedResponse<StarshipDto> searchByName(String name, int page, int limit) {
        SwapiListResponse<StarshipProperties> response = swapiClient.get(
                "/api/starships/?name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8),
                new ParameterizedTypeReference<>() {}
        );

        List<StarshipDto> allMatches = response.getResult().stream()
                .map(e -> mapToDto(e.getUid(), e.getProperties()))
                .toList();

        return PaginationUtils.paginate(allMatches, page, limit);
    }

    private StarshipDto mapToDto(String uid, StarshipProperties p) {
        return new StarshipDto(
                uid,
                p.getName(),
                p.getModel(),
                p.getStarshipClass(),
                p.getManufacturer(),
                p.getCostInCredits(),
                p.getLength(),
                p.getCrew(),
                p.getPassengers(),
                p.getMaxAtmospheringSpeed(),
                p.getHyperdriveRating(),
                p.getMglt(),
                p.getCargoCapacity(),
                p.getConsumables(),
                p.getFilms(),
                p.getPilots(),
                p.getUrl()
        );
    }

}