package com.portfolio.weatherintel.provider;

import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;

/**
 * Strategy interface for an external weather data source.
 *
 * Every implementation is responsible for calling its specific external API
 * and mapping the provider-specific response shape into this application's
 * normalized {@link WeatherSnapshot} / {@link AirQualitySnapshot} domain
 * model. No caller outside the {@code provider} package should ever need to
 * know which concrete provider answered a request - that decoupling is the
 * entire point of this interface, and it is what lets
 * {@code WeatherAggregationService} swap providers on failure without any
 * change to the recommendation engine or controllers.
 */
public interface WeatherProvider {

    /**
     * A short, human-readable name for logging and for tagging snapshots
     * with their data source (e.g. "Open-Meteo", "OpenWeatherMap").
     */
    String getProviderName();

    /**
     * Fetches current weather conditions for the given location.
     *
     * @throws ProviderUnavailableException if the provider cannot be reached,
     *         times out, or returns a response that cannot be parsed.
     */
    WeatherSnapshot fetchCurrentWeather(Location location);

    /**
     * Fetches current air quality conditions for the given location.
     *
     * @throws ProviderUnavailableException if the provider cannot be reached,
     *         times out, returns a response that cannot be parsed, or does
     *         not support air quality data.
     */
    AirQualitySnapshot fetchAirQuality(Location location);
}
