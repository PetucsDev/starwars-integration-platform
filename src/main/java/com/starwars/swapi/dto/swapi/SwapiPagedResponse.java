package com.starwars.swapi.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wraps the SWAPI paginated list response.
 *
 * <pre>
 * {
 *   "message": "ok",
 *   "total_records": "82",
 *   "total_pages": 9,
 *   "previous": null,
 *   "next": "https://...",
 *   "results": [ { "uid": "1", "name": "Luke Skywalker", "url": "..." } ]
 * }
 * </pre>
 *
 * Used for People, Starships and Vehicles list endpoints.
 */
public class SwapiPagedResponse {

    private String message;

    @JsonProperty("total_records")
    private String totalRecords;

    @JsonProperty("total_pages")
    private int totalPages;

    private String previous;
    private String next;
    private List<SwapiEntityRef> results;

    // =========================================================
    // Getters & Setters
    // =========================================================

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTotalRecords() { return totalRecords; }
    public void setTotalRecords(String totalRecords) { this.totalRecords = totalRecords; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public List<SwapiEntityRef> getResults() { return results; }
    public void setResults(List<SwapiEntityRef> results) { this.results = results; }
}