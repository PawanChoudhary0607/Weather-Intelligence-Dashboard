package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TravelSuitabilityRuleTest {

    private final RecommendationRuleProperties properties = new RecommendationRuleProperties();
    private final TravelSuitabilityRule rule = new TravelSuitabilityRule(properties);

    @Test
    void recommendsTravelInCalmConditions() {
        WeatherSnapshot weather = weatherWith(15.0, 20, 0.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void returnsCautionForHighWindAlone() {
        WeatherSnapshot weather = weatherWith(55.0, 20, 0.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
        assertThat(result.reasoning()).contains("wind");
    }

    @Test
    void returnsNotRecommendedForHeavyPrecipitationRegardlessOfWind() {
        WeatherSnapshot weather = weatherWith(10.0, 90, 15.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
        assertThat(result.reasoning()).contains("heavy precipitation");
    }

    @Test
    void doesNotFactorInAirQuality() {
        // Travel suitability is about disruption/safety, not comfort - confirm
        // that an extremely poor (but irrelevant) AQI value passed in does not
        // change the verdict for otherwise calm conditions.
        WeatherSnapshot weather = weatherWith(15.0, 20, 0.0);

        var resultWithoutAqi = rule.evaluate(weather, null);
        var resultWithBadAqi = rule.evaluate(weather,
                new com.portfolio.weatherintel.domain.AirQualitySnapshot(400, "Test", Instant.now()));

        assertThat(resultWithoutAqi.verdict()).isEqualTo(resultWithBadAqi.verdict());
    }

    private WeatherSnapshot weatherWith(double windKmh, int precipProbability, double precipAmount) {
        return new WeatherSnapshot(15.0, 15.0, windKmh, precipAmount, precipProbability, 50.0,
                "Test conditions", "Test", Instant.now());
    }
}
