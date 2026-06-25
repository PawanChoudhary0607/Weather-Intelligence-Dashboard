package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UmbrellaRuleTest {

    private final RecommendationRuleProperties properties = new RecommendationRuleProperties();
    private final UmbrellaRule rule = new UmbrellaRule(properties);

    @Test
    void recommendsUmbrellaWhenPrecipitationProbabilityIsHigh() {
        WeatherSnapshot weather = snapshot(20.0, 70, 0.2);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
        assertThat(result.reasoning()).contains("70%");
    }

    @Test
    void recommendsUmbrellaWhenPrecipitationAmountIsHighEvenIfProbabilityIsLow() {
        WeatherSnapshot weather = snapshot(20.0, 30, 2.5);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.RECOMMENDED);
    }

    @Test
    void returnsCautionForModerateButSubThresholdProbability() {
        WeatherSnapshot weather = snapshot(20.0, 30, 0.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.CAUTION);
    }

    @Test
    void returnsNotRecommendedWhenSkiesAreClear() {
        WeatherSnapshot weather = snapshot(20.0, 5, 0.0);

        var result = rule.evaluate(weather, null);

        assertThat(result.verdict()).isEqualTo(RecommendationVerdict.NOT_RECOMMENDED);
    }

    private WeatherSnapshot snapshot(double temp, int precipProbability, double precipAmount) {
        return new WeatherSnapshot(temp, temp, 10.0, precipAmount, precipProbability, 50.0,
                "Test conditions", "Test", Instant.now());
    }
}
