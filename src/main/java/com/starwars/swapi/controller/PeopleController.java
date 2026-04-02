package com.starwars.swapi.controller;

import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.PersonDto;
import com.starwars.swapi.service.PeopleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes endpoints for listing and retrieving Star Wars characters (People).
 *
 * <p>All endpoints require a valid JWT token in the {@code Authorization} header.
 */
@Validated
@RestController
@RequestMapping("/api/people")
@Tag(name = "People", description = "Browse Star Wars characters from the SWAPI")
@SecurityRequirement(name = "bearerAuth")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    /**
     * Returns a paginated list of Star Wars characters.
     *
     * <p>Use the optional {@code name} parameter to filter results by character name.
     *
     * @param page  page number, 1-based (default: 1)
     * @param limit number of items per page (default: 10)
     * @param name  optional name filter (e.g. "luke")
     * @return paginated response with people
     */
    @GetMapping
    @Operation(
            summary = "List people",
            description = "Returns a paginated list of Star Wars characters. "
                          + "Optionally filter by name (case-insensitive partial match via SWAPI search)."
    )
    public ResponseEntity<PagedResponse<PersonDto>> listPeople(
            @Parameter(description = "Page number (1-based)", example = "1")
            @Min(value = 1, message = "page must be >= 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Items per page", example = "10")
            @Min(value = 1, message = "limit must be >= 1")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Filter by character name", example = "luke")
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(peopleService.listPeople(page, limit, name));
    }

    /**
     * Returns full details for a single character by SWAPI ID.
     *
     * @param id the SWAPI character ID (e.g. "1" for Luke Skywalker)
     * @return full character details
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get person by ID",
            description = "Returns complete details for the character with the given SWAPI ID."
    )
    public ResponseEntity<PersonDto> getById(
            @Parameter(description = "SWAPI person ID", example = "1", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity.ok(peopleService.getById(id));
    }
}