package com.portfolio.weatherintel.provider;

import com.portfolio.weatherintel.config.WeatherProviderProperties;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import com.portfolio.weatherintel.provider.dto.OpenWeatherMapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

/**
 * Fallback weather data source, used only when {@link OpenMeteoProvider}
 * is unavailable. Requires an OpenWeatherMap API key configured via the
 * OPENWEATHERMAP_API_KEY environment variable.
 *
 * Important, documented limitation: OpenWeatherMap's free current-weather
 * endpoint does not expose a precipitation probability field (that level of
 * forecast detail requires a paid plan). When this provider is used,
 * precipitation probability is derived from whether precipitation is
 * currently being reported rather than a true forecast probability. This
 * is called out explicitly here and in the recommendation reasoning text
 * rather than silently presenting an invented number as real forecast data.
 *
 * This provider does not support air quality lookups in the MVP - air
 * quality requires OpenWeatherMap's separate Air Pollution API, which is
 * intentionally out of scope for the fallback path. If both weather and air
 * quality are needed and Open-Meteo is down, the air quality portion of the
 * dashboard degrades gracefully rather than this provider faking a value.
 */
@Component
public class OpenWeatherMapProvider implements WeatherProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherMapProvider.class);
    private static final String PROVIDER_NAME = "OpenWeatherMap";

    private final RestClient restClient;
    private final WeatherProviderProperties.OpenWeatherMap properties;

    public OpenWeatherMapProvider(RestClient openWeatherMapRestClient, WeatherProviderProperties providerProperties) {
        this.restClient = openWeatherMapRestClient;
        this.properties = providerProperties.getOpenWeatherMap();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public WeatherSnapshot fetchCurrentWeather(Location location) {
        if (!properties.isConfigured()) {
            throw new ProviderUnavailableException(
                    "OpenWeatherMap fallback is not configured - missing OPENWEATHERMAP_API_KEY");
        }

        String url = properties.getBaseUrl() + "/weather"
                + "?lat=" + location.latitude()
                + "&lon=" + location.longitude()
                + "&units=metric"
                + "&appid=" + properties.getApiKey();

        try {
            OpenWeatherMapResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenWeatherMapResponse.class);

            if (response == null || response.getMain() == null) {
                throw new ProviderUnavailableException("OpenWeatherMap returned an empty weather response");
            }

            double windSpeedKmh = response.getWind() != null ? response.getWind().getSpeedKmh() : 0.0;
            double rainAmount = response.getRain() != null && response.getRain().getOneHour() != null
                    ? response.getRain().getOneHour()
                    : 0.0;
            // Free tier has no true forecast probability; treat current rainfall
            // as a 100% "currently raining" signal, otherwise 0. This is a
            // documented approximation, not a fabricated forecast.
            int approximatedProbability = rainAmount > 0.0 ? 100 : 0;

            String description = response.getWeather() != null && !response.getWeather().isEmpty()
                    ? capitalize(response.getWeather().get(0).getDescription())
                    : "Conditions unavailable";

            Instant observedAt = response.getDt() != null
                    ? Instant.ofEpochSecond(response.getDt())
                    : Instant.now();

            return new WeatherSnapshot(
                    response.getMain().getTemp(),
                    response.getMain().getFeelsLike(),
                    windSpeedKmh,
                    rainAmount,
                    approximatedProbability,
                    response.getMain().getHumidity(),
                    description,
                    PROVIDER_NAME,
                    observedAt
            );
        } catch (RestClientException ex) {
            log.warn("OpenWeatherMap current weather request failed for location {}: {}", location.toCacheKey(), ex.getMessage());
            throw new ProviderUnavailableException("OpenWeatherMap current weather request failed", ex);
        }
    }

    @Override
    public AirQualitySnapshot fetchAirQuality(Location location) {
        throw new ProviderUnavailableException(
                "OpenWeatherMap fallback does not support air quality lookups in this application");
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
