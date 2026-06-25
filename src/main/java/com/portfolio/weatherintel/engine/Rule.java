package com.portfolio.weatherintel.engine;

import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.WeatherSnapshot;

/**
 * Strategy interface for a single recommendation rule.
 *
 * Each implementation evaluates current conditions for exactly one
 * {@link com.portfolio.weatherintel.domain.ActivityType} and returns a
 * {@link RuleResult} carrying both the verdict and a human-readable
 * explanation. Implementations are deliberately independent of one
 * another and of {@link RecommendationEngine} - adding a new recommendation
 * (e.g. a future "what to wear" rule) means writing one new class that
 * implements this interface and registering it as a Spring bean; no
 * existing code needs to change.
 *
 * {@code airQuality} may be {@code null} if air quality data could not be
 * retrieved (see {@code WeatherAggregationService}); rules that depend on
 * air quality must handle that gracefully rather than throwing.
 */
public interface Rule {

    RuleResult evaluate(WeatherSnapshot weather, AirQualitySnapshot airQuality);
}
