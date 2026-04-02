package com.starwars.swapi.util;

import com.starwars.swapi.dto.api.PagedResponse;

import java.util.List;

/**
 * Utility methods shared across SWAPI service classes.
 */
public final class PaginationUtils {

    private PaginationUtils() {}

    /**
     * Applies in-memory pagination to a full list of items.
     *
     * @param all   the complete (unfiltered/filtered) list
     * @param page  1-based page number
     * @param limit items per page
     * @param <T>   item type
     * @return a {@link PagedResponse} for the requested page
     */
    public static <T> PagedResponse<T> paginate(List<T> all, int page, int limit) {
        int total = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / limit));
        int fromIndex = Math.min((page - 1) * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);
        List<T> items = all.subList(fromIndex, toIndex);
        return PagedResponse.of(items, page, limit, totalPages, total,
                page < totalPages, page > 1);
    }

    /**
     * Safely parses a string as a {@code long}, returning {@code 0} on failure.
     *
     * @param value string to parse
     * @return parsed value or {@code 0}
     */
    public static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
