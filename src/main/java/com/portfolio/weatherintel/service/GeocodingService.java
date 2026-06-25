package com.portfolio.weatherintel.service;

import com.portfolio.weatherintel.config.CacheConfig;
import com.portfolio.weatherintel.config.WeatherProviderProperties;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.exception.LocationNotFoundException;
import com.portfolio.weatherintel.provider.dto.OpenMeteoGeocodingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * Resolves a free-text place name (e.g. "Paris") into a {@link Location}
 * using Open-Meteo's free geocoding API. Geocoding results for a given
 * place name are effectively static, so they are cached with a long TTL.
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    private final RestClient restClient;
    private final WeatherProviderProperties.OpenMeteo properties;

    public GeocodingService(RestClient openMeteoRestClient, WeatherProviderProperties providerProperties) {
        this.restClient = openMeteoRestClient;
        this.properties = providerProperties.getOpenMeteo();
    }

    @Cacheable(value = CacheConfig.GEOCODING_CACHE, key = "#placeName.toLowerCase()")
    public Location resolve(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            throw new LocationNotFoundException("A location name must be provided");
        }

        String encodedName = UriUtils.encodeQueryParam(placeName.trim(), StandardCharsets.UTF_8);
        String url = properties.getGeocodingUrl() + "/search?name=" + encodedName + "&count=1&language=en&format=json";

        try {
            OpenMeteoGeocodingResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenMeteoGeocodingResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                throw new LocationNotFoundException("No location found matching \"" + placeName + "\"");
            }

            var result = response.getResults().get(0);
            return new Location(result.getLatitude(), result.getLongitude(), result.toDisplayName());
        } catch (RestClientException ex) {
            log.warn("Geocoding request failed for place name '{}': {}", placeName, ex.getMessage());
            throw new LocationNotFoundException("Unable to resolve location \"" + placeName + "\" right now", ex);
        }
    }
}
