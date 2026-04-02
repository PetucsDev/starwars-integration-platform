package com.starwars.swapi.controller;

import com.starwars.swapi.dto.api.PagedResponse;
import com.starwars.swapi.dto.api.VehicleDto;
import com.starwars.swapi.service.VehiclesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes endpoints for listing and retrieving Star Wars vehicles.
 */
@Validated
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Browse Star Wars vehicles from the SWAPI")
@SecurityRequirement(name = "bearerAuth")
public class VehiclesController {

    private final VehiclesService vehiclesService;

    public VehiclesController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    /**
     * Returns a paginated list of Star Wars vehicles.
     *
     * @param page  page number, 1-based (default: 1)
     * @param limit items per page (default: 10)
     * @param name  optional name filter (e.g. "speeder")
     * @return paginated list of vehicles
     */
    @GetMapping
    @Operation(
            summary = "List vehicles",
            description = "Returns a paginated list of Star Wars vehicles. "
                          + "Optionally filter by name."
    )
    public ResponseEntity<PagedResponse<VehicleDto>> listVehicles(
            @Parameter(description = "Page number (1-based)", example = "1")
            @Min(value = 1, message = "page must be >= 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Items per page", example = "10")
            @Min(value = 1, message = "limit must be >= 1")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Filter by vehicle name", example = "speeder")
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(vehiclesService.listVehicles(page, limit, name));
    }

    /**
     * Returns full details for a single vehicle by SWAPI ID.
     *
     * @param id the SWAPI vehicle ID
     * @return full vehicle details
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get vehicle by ID",
            description = "Returns complete details for the vehicle with the given SWAPI ID."
    )
    public ResponseEntity<VehicleDto> getById(
            @Parameter(description = "SWAPI vehicle ID", example = "4", required = true)
            @PathVariable String id
    ) {
        return ResponseEntity.ok(vehiclesService.getById(id));
    }
}