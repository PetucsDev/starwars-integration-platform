package com.starwars.swapi.service;

import com.starwars.swapi.client.SwapiClient;
import com.starwars.swapi.dto.api.FilmDto;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.swapi.FilmProperties;
import com.starwars.swapi.dto.swapi.SwapiFullEntity;
import com.starwars.swapi.dto.swapi.SwapiListResponse;
import com.starwars.swapi.dto.swapi.SwapiSingleResponse;
import com.starwars.swapi.util.PaginationUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service that fetches and maps Films data from the Star Wars API.
 *
 * <p>SWAPI does not paginate films (there are only 6 canonical films),
 * so pagination is applied in-memory on the full result set.
 */
@Service
public class FilmsService {

    private final SwapiClient swapiClient;

    public FilmsService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    /**
     * Lists all films with optional title filtering and pagination.
     *
     * @param page  page number (1-based)
     * @param limit items per page
     * @param title optional title filter (case-insensitive substring match)
     * @return paginated list of films
     */
    @Cacheable(value = "films", key = "#page + '-' + #limit + '-' + #title")
    public PagedResponse<FilmDto> listFilms(int page, int limit, String title) {
        SwapiListResponse<FilmProperties> response = swapiClient.get(
                "/api/films",
                new ParameterizedTypeReference<>() {}
        );

        List<FilmDto> allFilms = response.getResult().stream()
                .map(e -> mapToSummaryDto(e.getUid(), e.getProperties()))
                .toList();

        if (StringUtils.hasText(title)) {
            String lowerTitle = title.toLowerCase();
            allFilms = allFilms.stream()
                    .filter(f -> f.title() != null && f.title().toLowerCase().contains(lowerTitle))
                    .toList();
        }

        return PaginationUtils.paginate(allFilms, page, limit);
    }

    /**
     * Retrieves a single film by its SWAPI ID.
     *
     * @param id the SWAPI film ID
     * @return full film details
     */
    @Cacheable(value = "films", key = "'id-' + #id")
    public FilmDto getById(String id) {
        SwapiSingleResponse<FilmProperties> response = swapiClient.get(
                "/api/films/" + id,
                new ParameterizedTypeReference<>() {}
        );
        SwapiFullEntity<FilmProperties> entity = response.getResult();
        return mapToFullDto(entity.getUid(), entity.getProperties());
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private FilmDto mapToSummaryDto(String uid, FilmProperties p) {
        return FilmDto.summary(uid, p.getTitle(), p.getEpisodeId(),
                p.getDirector(), p.getReleaseDate(), p.getUrl());
    }

    private FilmDto mapToFullDto(String uid, FilmProperties p) {
        return new FilmDto(
                uid,
                p.getTitle(),
                p.getEpisodeId(),
                p.getDirector(),
                p.getProducer(),
                p.getReleaseDate(),
                p.getOpeningCrawl(),
                p.getCharacters(),
                p.getPlanets(),
                p.getStarships(),
                p.getVehicles(),
                p.getSpecies(),
                p.getUrl()
        );
    }

}