package com.starwars.swapi.service;

import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.PersonDto;
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
 * Service that fetches and maps People data from the Star Wars API.
 */
@Service
public class PeopleService {

    private final SwapiClient swapiClient;

    public PeopleService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    /**
     * Lists people with optional name filtering.
     *
     * <p>If {@code name} is provided, the SWAPI search endpoint is called and
     * pagination is applied locally. Otherwise, SWAPI's native pagination is used.
     *
     * @param page  page number (1-based)
     * @param limit items per page
     * @param name  optional name filter
     * @return paginated list of people
     */
    @Cacheable(value = "people", key = "#page + '-' + #limit + '-' + #name")
    public PagedResponse<PersonDto> listPeople(int page, int limit, String name) {
        if (StringUtils.hasText(name)) {
            return searchByName(name, page, limit);
        }
        return fetchPage(page, limit);
    }

    /**
     * Retrieves a single person by their SWAPI ID.
     *
     * @param id the SWAPI person ID
     * @return full person details
     */
    @Cacheable(value = "people", key = "'id-' + #id")
    public PersonDto getById(String id) {
        SwapiSingleResponse<PersonProperties> response = swapiClient.get(
                "/api/people/" + id,
                new ParameterizedTypeReference<>() {}
        );
        SwapiFullEntity<PersonProperties> entity = response.getResult();
        return mapToDto(entity.getUid(), entity.getProperties());
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private PagedResponse<PersonDto> fetchPage(int page, int limit) {
        SwapiPagedResponse response = swapiClient.get(
                "/api/people?page=" + page + "&limit=" + limit,
                new ParameterizedTypeReference<>() {}
        );

        List<PersonDto> items = response.getResults().stream()
                .map(ref -> PersonDto.summary(ref.uid(), ref.name(), ref.url()))
                .toList();

        long totalRecords = PaginationUtils.parseLong(response.getTotalRecords());
        return PagedResponse.of(
                items, page, limit,
                response.getTotalPages(), totalRecords,
                response.getNext() != null,
                response.getPrevious() != null
        );
    }

    private PagedResponse<PersonDto> searchByName(String name, int page, int limit) {
        SwapiListResponse<PersonProperties> response = swapiClient.get(
                "/api/people/?name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8),
                new ParameterizedTypeReference<>() {}
        );

        List<PersonDto> allMatches = response.getResult().stream()
                .map(e -> mapToDto(e.getUid(), e.getProperties()))
                .toList();

        return PaginationUtils.paginate(allMatches, page, limit);
    }

    private PersonDto mapToDto(String uid, PersonProperties p) {
        return new PersonDto(
                uid,
                p.getName(),
                p.getHeight(),
                p.getMass(),
                p.getHairColor(),
                p.getSkinColor(),
                p.getEyeColor(),
                p.getBirthYear(),
                p.getGender(),
                p.getHomeworld(),
                p.getUrl()
        );
    }

}