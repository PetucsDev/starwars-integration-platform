package com.starwars.swapi.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object representing a Star Wars character.
 *
 * <p>Fields marked with {@code @JsonInclude(NON_NULL)} are omitted from the
 * response when not populated (e.g. in summary list responses).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonDto(
        String uid,
        String name,
        String height,
        String mass,
        String hairColor,
        String skinColor,
        String eyeColor,
        String birthYear,
        String gender,
        String homeworld,
        String url
) {
    /** Creates a summary-only DTO (uid + name) for use in paginated list responses. */
    public static PersonDto summary(String uid, String name, String url) {
        return new PersonDto(uid, name, null, null, null, null, null, null, null, null, url);
    }
}