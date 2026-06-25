package com.portfolio.weatherintel.aggregation;

import com.portfolio.weatherintel.config.CacheConfig;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import com.portfolio.weatherintel.provider.OpenMeteoProvider;
import com.portfolio.weatherintel.provider.OpenWeatherMapProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Orchestrates weather and air quality lookups across providers.
 *
 * The fallback strategy is intentionally simple and linear, matching the
 * MVP scope: try the primary provider (Open-Meteo), and only if that fails
 * (timeout, network error, malformed response) fall back to the secondary
 * provider (OpenWeatherMap). There is no reconciliation or averaging
 * between providers - exactly one provider's data is used for any given
 * request, which keeps every recommendation traceable to a single,
 * citable data source rather than a blended number nobody can fully
 * explain.
 *
 * Air quality has no fallback provider in the MVP (see
 * {@link OpenWeatherMapProvider#fetchAirQuality}); if Open-Meteo cannot
 * supply it, the caller receives a {@link ProviderUnavailableException}
 * and the dashboard degrades that section gracefully rather than guessing.
 */
@Service
public class WeatherAggregationService {

    private static final Logger log = LoggerFactory.getLogger(WeatherAggregationService.class);

    private final OpenMeteoProvider primaryProvider;
    private final OpenWeatherMapProvider fallbackProvider;

    public WeatherAggregationService(OpenMeteoProvider primaryProvider, OpenWeatherMapProvider fallbackProvider) {
        this.primaryProvider = primaryProvider;
        this.fallbackProvider = fallbackProvider;
    }

    @Cacheable(value = CacheConfig.WEATHER_CACHE, key = "#location.toCacheKey()")
    public WeatherSnapshot getCurrentWeather(Location location) {
        try {
            return primaryProvider.fetchCurrentWeather(location);
        } catch (ProviderUnavailableException primaryFailure) {
            log.warn("Primary weather provider ({}) unavailable for {}, falling back to {}: {}",
                    primaryProvider.getProviderName(), location.toCacheKey(),
                    fallbackProvider.getProviderName(), primaryFailure.getMessage());

            try {
                return fallbackProvider.fetchCurrentWeather(location);
            } catch (ProviderUnavailableException fallbackFailure) {
                log.error("Fallback weather provider ({}) also unavailable for {}: {}",
                        fallbackProvider.getProviderName(), location.toCacheKey(), fallbackFailure.getMessage());
                throw new ProviderUnavailableException(
                        "Weather data is temporarily unavailable for this location. "
                                + "Both the primary and fallback providers failed to respond.",
                        fallbackFailure);
            }
        }
    }

    @Cacheable(value = CacheConfig.AIR_QUALITY_CACHE, key = "#location.toCacheKey()")
    public AirQualitySnapshot getAirQuality(Location location) {
        try {
            return primaryProvider.fetchAirQuality(location);
        } catch (ProviderUnavailableException primaryFailure) {
            log.warn("Air quality provider ({}) unavailable for {}: {}",
                    primaryProvider.getProviderName(), location.toCacheKey(), primaryFailure.getMessage());
            throw new ProviderUnavailableException(
                    "Air quality data is temporarily unavailable for this location.", primaryFailure);
        }
    }
}
