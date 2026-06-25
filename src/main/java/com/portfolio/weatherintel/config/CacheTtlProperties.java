package com.portfolio.weatherintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for the {@code weather.cache} section of application.yml.
 * Read by {@link CacheConfig} when constructing each named Caffeine cache, so
 * the documented TTL in configuration is the TTL actually in effect rather
 * than a value that only describes intent.
 */
@ConfigurationProperties(prefix = "weather.cache")
public class CacheTtlProperties {

    private long weatherTtlMinutes = 15;
    private long airQualityTtlMinutes = 45;
    private long geocodingTtlMinutes = 1440;

    public long getWeatherTtlMinutes() {
        return weatherTtlMinutes;
    }

    public void setWeatherTtlMinutes(long weatherTtlMinutes) {
        this.weatherTtlMinutes = weatherTtlMinutes;
    }

    public long getAirQualityTtlMinutes() {
        return airQualityTtlMinutes;
    }

    public void setAirQualityTtlMinutes(long airQualityTtlMinutes) {
        this.airQualityTtlMinutes = airQualityTtlMinutes;
    }

    public long getGeocodingTtlMinutes() {
        return geocodingTtlMinutes;
    }

    public void setGeocodingTtlMinutes(long geocodingTtlMinutes) {
        this.geocodingTtlMinutes = geocodingTtlMinutes;
    }
}
