package com.starwars.swapi.dto.swapi;

/**
 * Wraps the SWAPI single-entity response (e.g. {@code GET /api/people/1}).
 *
 * <pre>
 * {
 *   "message": "ok",
 *   "result": { "properties": { ... }, "uid": "1" }
 * }
 * </pre>
 *
 * @param <P> the type of the entity properties object
 */
public class SwapiSingleResponse<P> {

    private String message;
    private SwapiFullEntity<P> result;

    // =========================================================
    // Getters & Setters
    // =========================================================

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SwapiFullEntity<P> getResult() { return result; }
    public void setResult(SwapiFullEntity<P> result) { this.result = result; }
}