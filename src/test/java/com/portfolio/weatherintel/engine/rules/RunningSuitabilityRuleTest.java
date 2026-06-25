package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RunningSuitabilityRuleTest {

    private final RecommendationRuleProperties properties = new RecommendationRuleProperties();
    private final RunningSuitabilityRule rule = new RunningSuitabilityRule(properties);

    @Test
    void recommendsRunningInComfortableConditions() {
        WeatherSnapshot weather = weatherWith(18.0, 10.0);
        AirQualitySnapshot aqi = airQuality(40);

        var result = rule.evaluate(weather, aqi);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void returnsCautionForExactlyOneTriggeredFactor() {
        WeatherSnapshot weather = weatherWith(32.0, 10.0); // hot, but wind/AQI fine
        AirQualitySnapshot aqi = airQuality(40);

        var result = rule.evaluate(weather, aqi);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
        assertThat(result.reasoning()).contains("hot");
    }

    @Test
    void returnsNotRecommendedWhenMultipleFactorsAreTriggered() {
        WeatherSnapshot weather = weatherWith(33.0, 40.0); // hot AND windy
        AirQualitySnapshot aqi = airQuality(150); // and poor AQI

        var result = rule.evaluate(weather, aqi);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
        assertThat(result.reasoning()).contains("hot").contains("wind").contains("AQI");
    }

    @Test
    void treatsNullAirQualityAsNonTriggering() {
        WeatherSnapshot weather = weatherWith(18.0, 10.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void flagsColdTemperatures() {
        WeatherSnapshot weather = weatherWith(-2.0, 10.0);

        var result = rule.evaluate(weather, airQuality(30));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
        assertThat(result.reasoning()).contains("cold");
    }

    private WeatherSnapshot weatherWith(double feelsLike, double windKmh) {
        return new WeatherSnapshot(feelsLike, feelsLike, windKmh, 0.0, 0, 50.0,
                "Test conditions", "Test", Instant.now());
    }

    private AirQualitySnapshot airQuality(int aqi) {
        return new AirQualitySnapshot(aqi, "Test", Instant.now());
    }
}
