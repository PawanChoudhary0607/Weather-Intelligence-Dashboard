package com.portfolio.weatherintel.aggregation;

import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import com.portfolio.weatherintel.provider.OpenMeteoProvider;
import com.portfolio.weatherintel.provider.OpenWeatherMapProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherAggregationServiceTest {

    private OpenMeteoProvider primaryProvider;
    private OpenWeatherMapProvider fallbackProvider;
    private WeatherAggregationService service;
    private final Location testLocation = new Location(51.5074, -0.1278, "London");

    @BeforeEach
    void setUp() {
        primaryProvider = mock(OpenMeteoProvider.class);
        fallbackProvider = mock(OpenWeatherMapProvider.class);
        service = new WeatherAggregationService(primaryProvider, fallbackProvider);
    }

    @Test
    void usesPrimaryProviderWhenItSucceeds() {
        WeatherSnapshot expected = snapshot("Open-Meteo");
        when(primaryProvider.fetchCurrentWeather(any())).thenReturn(expected);

        WeatherSnapshot actual = service.getCurrentWeather(testLocation);

        assertThat(actual).isEqualTo(expected);
        verify(fallbackProvider, never()).fetchCurrentWeather(any());
    }

    @Test
    void fallsBackToSecondaryProviderWhenPrimaryFails() {
        when(primaryProvider.fetchCurrentWeather(any()))
                .thenThrow(new ProviderUnavailableException("primary down"));
        WeatherSnapshot fallbackResult = snapshot("OpenWeatherMap");
        when(fallbackProvider.fetchCurrentWeather(any())).thenReturn(fallbackResult);

        WeatherSnapshot actual = service.getCurrentWeather(testLocation);

        assertThat(actual.sourceProvider()).isEqualTo("OpenWeatherMap");
    }

    @Test
    void throwsWhenBothProvidersFail() {
        when(primaryProvider.fetchCurrentWeather(any()))
                .thenThrow(new ProviderUnavailableException("primary down"));
        when(fallbackProvider.fetchCurrentWeather(any()))
                .thenThrow(new ProviderUnavailableException("fallback down"));

        assertThatThrownBy(() -> service.getCurrentWeather(testLocation))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void airQualityHasNoFallbackAndPropagatesFailure() {
        when(primaryProvider.fetchAirQuality(any()))
                .thenThrow(new ProviderUnavailableException("primary down"));

        assertThatThrownBy(() -> service.getAirQuality(testLocation))
                .isInstanceOf(ProviderUnavailableException.class);
        verify(fallbackProvider, never()).fetchAirQuality(any());
    }

    @Test
    void returnsAirQualityFromPrimaryWhenAvailable() {
        AirQualitySnapshot expected = new AirQualitySnapshot(42, "Open-Meteo", Instant.now());
        when(primaryProvider.fetchAirQuality(any())).thenReturn(expected);

        AirQualitySnapshot actual = service.getAirQuality(testLocation);

        assertThat(actual).isEqualTo(expected);
    }

    private WeatherSnapshot snapshot(String provider) {
        return new WeatherSnapshot(20.0, 19.0, 10.0, 0.0, 10, 50.0, "Clear", provider, Instant.now());
    }
}
