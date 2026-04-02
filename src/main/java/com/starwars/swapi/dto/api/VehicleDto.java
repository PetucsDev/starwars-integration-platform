package com.starwars.swapi.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Data Transfer Object representing a Star Wars vehicle.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehicleDto(
        String uid,
        String name,
        String model,
        String vehicleClass,
        String manufacturer,
        String costInCredits,
        String length,
        String crew,
        String passengers,
        String maxAtmospheringSpeed,
        String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {
    /** Creates a summary-only DTO for paginated list responses. */
    public static VehicleDto summary(String uid, String name, String url) {
        return new VehicleDto(uid, name, null, null, null, null, null, null, null,
                null, null, null, null, null, url);
    }
}