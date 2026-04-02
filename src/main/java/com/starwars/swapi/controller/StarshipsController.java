package com.starwars.swapi.controller;

import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.StarshipDto;
import com.starwars.swapi.service.StarshipsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes endpoints for listing and retrieving Star Wars starships.
 */
@Validated
@RestController
@RequestMapping("/api/starships")
@Tag(name = "Starships", description = "Browse Star Wars starships from the SWAPI")
@SecurityRequirement(name = "bearerAuth")
public class StarshipsController {

    private final StarshipsService starshipsService;

    public StarshipsController(StarshipsService starshipsService) {
        this.starshipsService = starshipsService;
    }

    /**
     * Returns a paginated list of Star Wars starships.
     *
     * @param page  page number, 1-based (default: 1)
     * @param limit items per page (default: 10)
     * @param name  optional name filter (e.g. "falcon")
     * @return paginated list of starships
     */
    @GetMapping
    @Operation(
            summary = "List starships",
            description = "Returns a paginated list of Star Wars starships. "
                          + "Optionally filter by name."
    )
    public ResponseEntity<PagedResponse<StarshipDto>> listStarships(
            @Parameter(description = "Page number (1-based)", example = "1")
            @Min(value = 1, message = "page must be >= 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Items per page", example = "10")
            @Min(value = 1, message = "limit must be >= 1")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Filter by starship name", example = "falcon")
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(starshipsService.listStarships(page, limit, name));
    }

    /**
     * Returns full details for a single starship by SWAPI ID.
     *
     * @param id the SWAPI starship ID
     * @return full starship details
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get starship by ID",
            description = "Returns complete details for the starship with the given SWAPI ID."
    )
    public ResponseEntity<StarshipDto> getById(
            @Parameter(description = "SWAPI starship ID", example = "9", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity.ok(starshipsService.getById(id));
    }
}