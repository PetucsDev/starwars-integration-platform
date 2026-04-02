package com.starwars.swapi.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Data Transfer Object representing a Star Wars film.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilmDto(
        String uid,
        String title,
        Integer episodeId,
        String director,
        String producer,
        String releaseDate,
        String openingCrawl,
        List<String> characters,
        List<String> planets,
        List<String> starships,
        List<String> vehicles,
        List<String> species,
        String url
) {
    /** Creates a summary-only DTO for list responses. */
    public static FilmDto summary(String uid, String title, Integer episodeId,
                                  String director, String releaseDate, String url) {
        return new FilmDto(uid, title, episodeId, director, null, releaseDate,
                null, null, null, null, null, null, url);
    }
}