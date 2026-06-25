package com.portfolio.weatherintel.provider;

import com.portfolio.weatherintel.config.WeatherProviderProperties;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoProviderTest {

    private MockRestServiceServer mockServer;
    private OpenMeteoProvider provider;
    private final Location testLocation = new Location(51.5074, -0.1278, "London");

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        WeatherProviderProperties properties = new WeatherProviderProperties();
        properties.getOpenMeteo().setBaseUrl("https://api.open-meteo.com/v1");
        properties.getOpenMeteo().setAirQualityUrl("https://air-quality-api.open-meteo.com/v1");

        provider = new OpenMeteoProvider(builder.build(), properties);
    }

    @Test
    void mapsCurrentWeatherResponseToNormalizedSnapshot() {
        String json = """
                {
                  "latitude": 51.5,
                  "longitude": -0.13,
                  "current": {
                    "time": "2026-06-19T10:00",
                    "temperature_2m": 19.4,
                    "apparent_temperature": 18.1,
                    "relative_humidity_2m": 62.0,
                    "precipitation": 0.0,
                    "wind_speed_10m": 14.5,
                    "weather_code": 1
                  },
                  "hourly": {
                    "time": ["2026-06-19T09:00", "2026-06-19T10:00", "2026-06-19T11:00"],
                    "precipitation_probability": [10, 25, 40]
                  }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/forecast")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var snapshot = provider.fetchCurrentWeather(testLocation);

        assertThat(snapshot.temperatureCelsius()).isEqualTo(19.4);
        assertThat(snapshot.feelsLikeCelsius()).isEqualTo(18.1);
        assertThat(snapshot.windSpeedKmh()).isEqualTo(14.5);
        assertThat(snapshot.precipitationProbabilityPercent()).isEqualTo(25);
        assertThat(snapshot.conditionDescription()).isEqualTo("Partly cloudy");
        assertThat(snapshot.sourceProvider()).isEqualTo("Open-Meteo");
    }

    @Test
    void mapsAirQualityResponseToNormalizedSnapshot() {
        String json = """
                {
                  "current": {
                    "time": "2026-06-19T10:00",
                    "us_aqi": 42
                  }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/air-quality")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var snapshot = provider.fetchAirQuality(testLocation);

        assertThat(snapshot.aqi()).isEqualTo(42);
        assertThat(snapshot.sourceProvider()).isEqualTo("Open-Meteo");
    }

    @Test
    void throwsProviderUnavailableExceptionOnServerError() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/forecast")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> provider.fetchCurrentWeather(testLocation))
                .isInstanceOf(ProviderUnavailableException.class);
    }
}
