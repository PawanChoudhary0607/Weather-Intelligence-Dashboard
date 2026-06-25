package com.portfolio.weatherintel.exception;

/**
 * Thrown when a {@code WeatherProvider} cannot return a usable result -
 * whether due to a network failure, a timeout, a non-2xx response, or
 * malformed data. The aggregation service catches this per-provider to
 * decide whether to fall back to the next provider or to cached data.
 */
public class ProviderUnavailableException extends RuntimeException {

    public ProviderUnavailableException(String message) {
        super(message);
    }

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
