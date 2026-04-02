package com.starwars.swapi.dto.swapi;

import java.util.List;

/**
 * Wraps SWAPI responses where {@code result} is a list of full entities.
 *
 * <p>Used for:
 * <ul>
 *   <li>{@code GET /api/films} — returns all films</li>
 *   <li>{@code GET /api/people/?name=luke} — name-search results</li>
 *   <li>{@code GET /api/starships/?name=falcon}</li>
 *   <li>{@code GET /api/vehicles/?name=sand}</li>
 * </ul>
 *
 * <pre>
 * {
 *   "message": "ok",
 *   "result": [ { "properties": { ... }, "uid": "1" }, ... ]
 * }
 * </pre>
 *
 * @param <P> the type of the entity properties object
 */
public class SwapiListResponse<P> {

    private String message;
    private List<SwapiFullEntity<P>> result;

    // =========================================================
    // Getters & Setters
    // =========================================================

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<SwapiFullEntity<P>> getResult() { return result; }
    public void setResult(List<SwapiFullEntity<P>> result) { this.result = result; }
}