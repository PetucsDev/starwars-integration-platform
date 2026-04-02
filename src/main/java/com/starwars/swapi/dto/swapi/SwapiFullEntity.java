package com.starwars.swapi.dto.swapi;

/**
 * Wraps a full SWAPI entity (with its {@code properties} object) returned
 * in single-entity, all-films, and name-search responses.
 *
 * <pre>
 * {
 *   "properties": { ... },
 *   "description": "A person within the Star Wars universe",
 *   "uid": "1"
 * }
 * </pre>
 *
 * @param <P> the type of the {@code properties} object
 */
public class SwapiFullEntity<P> {

    private P properties;
    private String description;
    private String uid;

    // =========================================================
    // Getters & Setters
    // =========================================================

    public P getProperties() { return properties; }
    public void setProperties(P properties) { this.properties = properties; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
}