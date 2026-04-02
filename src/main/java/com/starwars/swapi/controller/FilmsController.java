package com.starwars.swapi.controller;

import com.starwars.swapi.dto.api.FilmDto;
import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.service.FilmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes endpoints for listing and retrieving Star Wars films.
 */
@Validated
@RestController
@RequestMapping("/api/films")
@Tag(name = "Films", description = "Browse Star Wars films from the SWAPI")
@SecurityRequirement(name = "bearerAuth")
public class FilmsController {

    private final FilmsService filmsService;

    public FilmsController(FilmsService filmsService) {
        this.filmsService = filmsService;
    }

    /**
     * Returns a paginated list of Star Wars films.
     *
     * <p>SWAPI does not paginate films natively — this endpoint paginates the
     * full list in-memory. An optional {@code title} parameter filters by
     * film title (case-insensitive substring match).
     *
     * @param page  page number, 1-based (default: 1)
     * @param limit items per page (default: 6)
     * @param title optional title filter
     * @return paginated list of films
     */
    @GetMapping
    @Operation(
            summary = "List films",
            description = "Returns a paginated list of Star Wars films. "
                          + "Use the optional 'title' parameter to filter by film title."
    )
    public ResponseEntity<PagedResponse<FilmDto>> listFilms(
            @Parameter(description = "Page number (1-based)", example = "1")
            @Min(value = 1, message = "page must be >= 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Items per page", example = "6")
            @Min(value = 1, message = "limit must be >= 1")
            @RequestParam(defaultValue = "6") int limit,

            @Parameter(description = "Filter by film title", example = "hope")
            @RequestParam(required = false) String title
    ) {
        return ResponseEntity.ok(filmsService.listFilms(page, limit, title));
    }

    /**
     * Returns full details for a single film by SWAPI ID.
     *
     * @param id the SWAPI film ID (e.g. "1" for A New Hope)
     * @return full film details including characters, planets, starships, vehicles
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get film by ID",
            description = "Returns complete details for the film with the given SWAPI ID, "
                          + "including all related entity URLs."
    )
    public ResponseEntity<FilmDto> getById(
            @Parameter(description = "SWAPI film ID", example = "1", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity.ok(filmsService.getById(id));
    }
}