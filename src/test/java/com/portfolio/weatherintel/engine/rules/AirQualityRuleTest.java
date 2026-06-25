package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AirQualityRuleTest {

    private final RecommendationRuleProperties properties = new RecommendationRuleProperties();
    private final AirQualityRule rule = new AirQualityRule(properties);
    private final WeatherSnapshot anyWeather = new WeatherSnapshot(
            20.0, 20.0, 5.0, 0.0, 0, 50.0, "Clear", "Test", Instant.now());

    @Test
    void returnsRecommendedForGoodAqi() {
        var result = rule.evaluate(anyWeather, snapshot(35));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
        assertThat(result.reasoning()).contains("Good");
    }

    @Test
    void returnsRecommendedForModerateAqi() {
        var result = rule.evaluate(anyWeather, snapshot(80));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
        assertThat(result.reasoning()).contains("Moderate");
    }

    @Test
    void returnsCautionForUnhealthyForSensitiveGroups() {
        var result = rule.evaluate(anyWeather, snapshot(130));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
    }

    @Test
    void returnsNotRecommendedForUnhealthyAqi() {
        var result = rule.evaluate(anyWeather, snapshot(180));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
    }

    @Test
    void returnsNotRecommendedForVeryUnhealthyOrWorse() {
        var result = rule.evaluate(anyWeather, snapshot(350));

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
        assertThat(result.reasoning()).contains("Very Unhealthy");
    }

    @Test
    void returnsCautionWhenAirQualityDataIsUnavailable() {
        var result = rule.evaluate(anyWeather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
        assertThat(result.reasoning()).contains("unavailable");
    }

    private AirQualitySnapshot snapshot(int aqi) {
        return new AirQualitySnapshot(aqi, "Test", Instant.now());
    }
}
