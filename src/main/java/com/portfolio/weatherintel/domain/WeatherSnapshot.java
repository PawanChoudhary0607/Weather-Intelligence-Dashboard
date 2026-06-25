package com.portfolio.weatherintel.domain;

import java.time.Instant;

/**
 * A normalized snapshot of current weather conditions for a location.
 *
 * This is the internal contract that every {@code WeatherProvider}
 * implementation maps its raw, provider-specific response into. No code
 * outside the {@code provider} package should ever see a raw Open-Meteo or
 * OpenWeatherMap response shape - the recommendation engine, the
 * aggregation service, and the controllers only ever work with this type.
 *
 * Units are fixed and documented per field so that rules can rely on them
 * without ambiguity:
 *  - temperature in Celsius
 *  - wind speed in km/h
 *  - precipitation amount in millimeters
 *  - precipitation probability as a percentage (0-100)
 */
public record WeatherSnapshot(
        double temperatureCelsius,
        double feelsLikeCelsius,
        double windSpeedKmh,
        double precipitationAmountMm,
        int precipitationProbabilityPercent,
        double humidityPercent,
        String conditionDescription,
        String sourceProvider,
        Instant observedAt
) {
}
