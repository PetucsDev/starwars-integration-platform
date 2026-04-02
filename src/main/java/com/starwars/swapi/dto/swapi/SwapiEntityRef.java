package com.starwars.swapi.dto.swapi;

/**
 * Minimal entity reference returned in paginated SWAPI list responses.
 * Contains only uid, name and the entity's own URL.
 */
public record SwapiEntityRef(String uid, String name, String url) {}