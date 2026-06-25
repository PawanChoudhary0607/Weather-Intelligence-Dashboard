package com.portfolio.weatherintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for the {@code weather.providers} section of application.yml.
 * Keeping provider URLs, keys, and timeouts here (rather than scattered
 * {@code @Value} annotations) gives a single, IDE-discoverable source of truth
 * for everything related to outbound provider calls.
 */
@ConfigurationProperties(prefix = "weather.providers")
public class WeatherProviderProperties {

    private OpenMeteo openMeteo = new OpenMeteo();
    private OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    public OpenMeteo getOpenMeteo() {
        return openMeteo;
    }

    public void setOpenMeteo(OpenMeteo openMeteo) {
        this.openMeteo = openMeteo;
    }

    public OpenWeatherMap getOpenWeatherMap() {
        return openWeatherMap;
    }

    public void setOpenWeatherMap(OpenWeatherMap openWeatherMap) {
        this.openWeatherMap = openWeatherMap;
    }

    public static class OpenMeteo {
        private String baseUrl;
        private String geocodingUrl;
        private String airQualityUrl;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getGeocodingUrl() {
            return geocodingUrl;
        }

        public void setGeocodingUrl(String geocodingUrl) {
            this.geocodingUrl = geocodingUrl;
        }

        public String getAirQualityUrl() {
            return airQualityUrl;
        }

        public void setAirQualityUrl(String airQualityUrl) {
            this.airQualityUrl = airQualityUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }

    public static class OpenWeatherMap {
        private String baseUrl;
        private String apiKey;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        /**
         * The OpenWeatherMap fallback is only usable if an API key has been configured.
         * Used by the provider and by health/diagnostic logging to fail fast and clearly
         * rather than send a request that OpenWeatherMap will reject.
         */
        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
