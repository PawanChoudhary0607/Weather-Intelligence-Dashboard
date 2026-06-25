package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OutdoorActivityRuleTest {

    private final RecommendationRuleProperties properties = new RecommendationRuleProperties();
    private final OutdoorActivityRule rule = new OutdoorActivityRule(properties);

    @Test
    void recommendsOutdoorActivityInPleasantConditions() {
        WeatherSnapshot weather = weatherWith(22.0, 10);

        var result = rule.evaluate(weather, airQuality(40));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void toleratesWiderTemperatureRangeThanRunning() {
        // 33C would be a CAUTION/NOT_RECOMMENDED trigger for running but is within
        // the outdoor-activity comfort band (up to 35C), demonstrating the two
        // rules intentionally use different thresholds for the same raw input.
        WeatherSnapshot weather = weatherWith(33.0, 10);

        var result = rule.evaluate(weather, airQuality(40));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void returnsNotRecommendedWhenRainAndPoorAirQualityCombine() {
        WeatherSnapshot weather = weatherWith(20.0, 80);

        var result = rule.evaluate(weather, airQuality(170));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
        assertThat(result.reasoning()).contains("precipitation").contains("AQI");
    }

    private WeatherSnapshot weatherWith(double feelsLike, int precipProbability) {
        return new WeatherSnapshot(feelsLike, feelsLike, 5.0, 0.0, precipProbability, 50.0,
                "Test conditions", "Test", Instant.now());
    }

    private AirQualitySnapshot airQuality(int aqi) {
        return new AirQualitySnapshot(aqi, "Test", Instant.now());
    }
}
