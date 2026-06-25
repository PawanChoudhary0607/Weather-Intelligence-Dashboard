package com.portfolio.weatherintel.domain;

import java.time.Instant;

/**
 * A normalized snapshot of air quality conditions for a location.
 *
 * {@code aqi} is normalized to the US EPA Air Quality Index scale
 * (0-500), which is the scale {@code AirQualityRule} and other rules
 * reason about. Open-Meteo's air quality endpoint provides this directly
 * as {@code us_aqi}.
 */
public record AirQualitySnapshot(
        int aqi,
        String sourceProvider,
        Instant observedAt
) {
}
