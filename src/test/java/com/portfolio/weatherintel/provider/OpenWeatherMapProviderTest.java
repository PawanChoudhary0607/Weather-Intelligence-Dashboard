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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherMapProviderTest {

    private MockRestServiceServer mockServer;
    private OpenWeatherMapProvider provider;
    private final Location testLocation = new Location(51.5074, -0.1278, "London");

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        WeatherProviderProperties properties = new WeatherProviderProperties();
        properties.getOpenWeatherMap().setBaseUrl("https://api.openweathermap.org/data/2.5");
        properties.getOpenWeatherMap().setApiKey("test-api-key");

        provider = new OpenWeatherMapProvider(builder.build(), properties);
    }

    @Test
    void mapsCurrentWeatherResponseToNormalizedSnapshotAndConvertsWindSpeedToKmh() {
        String json = """
                {
                  "main": { "temp": 21.0, "feels_like": 20.2, "humidity": 55 },
                  "wind": { "speed": 5.0 },
                  "weather": [ { "main": "Clear", "description": "clear sky" } ],
                  "dt": 1750000000
                }
                """;

        mockServer.expect(requestTo(containsString("/weather")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var snapshot = provider.fetchCurrentWeather(testLocation);

        assertThat(snapshot.temperatureCelsius()).isEqualTo(21.0);
        // 5.0 m/s * 3.6 = 18.0 km/h
        assertThat(snapshot.windSpeedKmh()).isEqualTo(18.0);
        assertThat(snapshot.conditionDescription()).isEqualTo("Clear sky");
        assertThat(snapshot.sourceProvider()).isEqualTo("OpenWeatherMap");
    }

    @Test
    void approximatesPrecipitationProbabilityFromCurrentRainfall() {
        String json = """
                {
                  "main": { "temp": 14.0, "feels_like": 13.0, "humidity": 80 },
                  "wind": { "speed": 2.0 },
                  "rain": { "1h": 1.2 },
                  "weather": [ { "main": "Rain", "description": "light rain" } ],
                  "dt": 1750000000
                }
                """;

        mockServer.expect(requestTo(containsString("/weather")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var snapshot = provider.fetchCurrentWeather(testLocation);

        assertThat(snapshot.precipitationAmountMm()).isEqualTo(1.2);
        assertThat(snapshot.precipitationProbabilityPercent()).isEqualTo(100);
    }

    @Test
    void throwsProviderUnavailableExceptionWhenApiKeyIsMissing() {
        WeatherProviderProperties unconfigured = new WeatherProviderProperties();
        unconfigured.getOpenWeatherMap().setBaseUrl("https://api.openweathermap.org/data/2.5");
        // apiKey deliberately left unset

        OpenWeatherMapProvider unconfiguredProvider =
                new OpenWeatherMapProvider(RestClient.builder().build(), unconfigured);

        assertThatThrownBy(() -> unconfiguredProvider.fetchCurrentWeather(testLocation))
                .isInstanceOf(ProviderUnavailableException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void airQualityIsNotSupportedByThisProvider() {
        assertThatThrownBy(() -> provider.fetchAirQuality(testLocation))
                .isInstanceOf(ProviderUnavailableException.class);
    }
}
