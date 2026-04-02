package com.starwars.swapi.dto.api;

import java.util.List;

/**
 * Generic paginated response wrapper returned to API consumers.
 *
 * @param <T> the type of items in this page
 */
public record PagedResponse<T>(
        List<T> items,
        int page,
        int limit,
        int totalPages,
        long totalRecords,
        boolean hasNext,
        boolean hasPrevious
) {
    /**
     * Builds a {@code PagedResponse} from SWAPI pagination metadata.
     *
     * @param items        current page items
     * @param page         current page number (1-based)
     * @param limit        page size
     * @param totalPages   total number of pages
     * @param totalRecords total number of records across all pages
     * @param hasNext      whether a next page exists
     * @param hasPrevious  whether a previous page exists
     * @param <T>          item type
     * @return a new {@code PagedResponse}
     */
    public static <T> PagedResponse<T> of(List<T> items, int page, int limit,
                                          int totalPages, long totalRecords,
                                          boolean hasNext, boolean hasPrevious) {
        return new PagedResponse<>(items, page, limit, totalPages, totalRecords, hasNext, hasPrevious);
    }
}