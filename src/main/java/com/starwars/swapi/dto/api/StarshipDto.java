package com.starwars.swapi.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Data Transfer Object representing a Star Wars starship.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StarshipDto(
        String uid,
        String name,
        String model,
        String starshipClass,
        String manufacturer,
        String costInCredits,
        String length,
        String crew,
        String passengers,
        String maxAtmospheringSpeed,
        String hyperdriveRating,
        String mglt,
        String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {
    /** Creates a summary-only DTO for paginated list responses. */
    public static StarshipDto summary(String uid, String name, String url) {
        return new StarshipDto(uid, name, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, url);
    }
}