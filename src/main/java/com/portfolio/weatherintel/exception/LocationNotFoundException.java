package com.portfolio.weatherintel.exception;

/**
 * Thrown when a place name cannot be resolved to coordinates via geocoding,
 * or when a user-supplied location is otherwise invalid.
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String message) {
        super(message);
    }

    public LocationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
