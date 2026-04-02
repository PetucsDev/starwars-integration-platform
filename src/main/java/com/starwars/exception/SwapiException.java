package com.starwars.exception;

/**
 * Thrown when the Star Wars external API returns an unexpected error
 * or is unavailable.
 */
public class SwapiException extends RuntimeException {
    public SwapiException(String message) {
        super(message);
    }

    public SwapiException(String message, Throwable cause) {
        super(message, cause);
    }
}