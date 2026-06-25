package com.portfolio.weatherintel.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Defines the application's in-memory caches.
 *
 * Different categories of weather data change at different rates, so each
 * cache gets its own time-to-live rather than sharing one blanket policy:
 * current weather conditions are volatile (short TTL), air quality changes
 * more slowly (medium TTL), and geocoding results for a given place name are
 * effectively static (long TTL).
 *
 * This is intentionally a single in-process cache (Caffeine) with no
 * external cache server. For a single-instance MVP deployment this is the
 * correct level of complexity; a multi-instance deployment would be the
 * trigger to introduce a shared cache such as Redis.
 */
@Configuration
public class CacheConfig {

    public static final String WEATHER_CACHE = "weatherCache";
    public static final String AIR_QUALITY_CACHE = "airQualityCache";
    public static final String GEOCODING_CACHE = "geocodingCache";

    @Bean
    public CacheManager cacheManager(CacheTtlProperties ttlProperties) {
        CaffeineCache weatherCache = new CaffeineCache(WEATHER_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(ttlProperties.getWeatherTtlMinutes(), TimeUnit.MINUTES)
                        .build());

        CaffeineCache airQualityCache = new CaffeineCache(AIR_QUALITY_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(ttlProperties.getAirQualityTtlMinutes(), TimeUnit.MINUTES)
                        .build());

        CaffeineCache geocodingCache = new CaffeineCache(GEOCODING_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(ttlProperties.getGeocodingTtlMinutes(), TimeUnit.MINUTES)
                        .build());

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(weatherCache, airQualityCache, geocodingCache));
        return cacheManager;
    }
}
