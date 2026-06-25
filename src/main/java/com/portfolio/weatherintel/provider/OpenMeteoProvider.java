package com.portfolio.weatherintel.provider;

import com.portfolio.weatherintel.config.WeatherProviderProperties;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import com.portfolio.weatherintel.provider.dto.OpenMeteoAirQualityResponse;
import com.portfolio.weatherintel.provider.dto.OpenMeteoForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Primary weather data source. Open-Meteo requires no API key and provides
 * current weather, hourly precipitation probability, and air quality data
 * all from the same provider family, which keeps this integration simple
 * and keeps the demo runnable by anyone who clones the repository without
 * needing to register for any credentials.
 */
@Component
public class OpenMeteoProvider implements WeatherProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoProvider.class);
    private static final String PROVIDER_NAME = "Open-Meteo";

    private final RestClient restClient;
    private final WeatherProviderProperties.OpenMeteo properties;

    public OpenMeteoProvider(RestClient openMeteoRestClient, WeatherProviderProperties providerProperties) {
        this.restClient = openMeteoRestClient;
        this.properties = providerProperties.getOpenMeteo();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public WeatherSnapshot fetchCurrentWeather(Location location) {
        String url = properties.getBaseUrl() + "/forecast"
                + "?latitude=" + location.latitude()
                + "&longitude=" + location.longitude()
                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,wind_speed_10m,weather_code"
                + "&hourly=precipitation_probability"
                + "&forecast_days=1"
                + "&timezone=auto";

        try {
            OpenMeteoForecastResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenMeteoForecastResponse.class);

            if (response == null || response.getCurrent() == null) {
                throw new ProviderUnavailableException("Open-Meteo returned an empty forecast response");
            }

            var current = response.getCurrent();
            int precipitationProbability = response.getHourly() != null
                    ? response.getHourly().findProbabilityForHour(current.getTime())
                    : 0;

            return new WeatherSnapshot(
                    current.getTemperature2m(),
                    current.getApparentTemperature(),
                    current.getWindSpeed10m(),
                    current.getPrecipitation(),
                    precipitationProbability,
                    current.getRelativeHumidity2m(),
                    describeWeatherCode(current.getWeatherCode()),
                    PROVIDER_NAME,
                    parseObservedAt(current.getTime())
            );
        } catch (RestClientException ex) {
            log.warn("Open-Meteo current weather request failed for location {}: {}", location.toCacheKey(), ex.getMessage());
            throw new ProviderUnavailableException("Open-Meteo current weather request failed", ex);
        }
    }

    @Override
    public AirQualitySnapshot fetchAirQuality(Location location) {
        String url = properties.getAirQualityUrl() + "/air-quality"
                + "?latitude=" + location.latitude()
                + "&longitude=" + location.longitude()
                + "&current=us_aqi"
                + "&timezone=auto";

        try {
            OpenMeteoAirQualityResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenMeteoAirQualityResponse.class);

            if (response == null || response.getCurrent() == null || response.getCurrent().getUsAqi() == null) {
                throw new ProviderUnavailableException("Open-Meteo returned an empty air quality response");
            }

            return new AirQualitySnapshot(
                    response.getCurrent().getUsAqi(),
                    PROVIDER_NAME,
                    parseObservedAt(response.getCurrent().getTime())
            );
        } catch (RestClientException ex) {
            log.warn("Open-Meteo air quality request failed for location {}: {}", location.toCacheKey(), ex.getMessage());
            throw new ProviderUnavailableException("Open-Meteo air quality request failed", ex);
        }
    }

    /**
     * Open-Meteo returns local time without a timezone offset suffix
     * (e.g. "2026-06-19T10:00"), since {@code timezone=auto} resolves it to
     * the location's local time. Parsing that exactly to an Instant requires
     * the UTC offset, which is not present in this field, so we fall back to
     * "now" - the precision lost here only affects the displayed "as of"
     * timestamp, not any rule's decision logic.
     */
    private Instant parseObservedAt(String openMeteoTime) {
        if (openMeteoTime == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(openMeteoTime + ":00Z");
        } catch (DateTimeParseException ex) {
            return Instant.now();
        }
    }

    /**
     * Maps Open-Meteo's WMO weather codes to a short human-readable
     * description. Only the common code ranges are mapped explicitly;
     * unmapped codes fall back to a generic label rather than failing.
     */
    private String describeWeatherCode(Integer code) {
        if (code == null) {
            return "Conditions unavailable";
        }
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snow fall";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Variable conditions";
        };
    }
}
