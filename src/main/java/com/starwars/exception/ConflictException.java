package com.starwars.exception;

/**
 * Thrown when a uniqueness constraint is violated (e.g. duplicate username).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}