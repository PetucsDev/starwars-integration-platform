package com.starwars.exception;

/**
 * Thrown when a requested Star Wars resource does not exist (SWAPI returns 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}