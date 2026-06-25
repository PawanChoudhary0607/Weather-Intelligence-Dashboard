package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.ActivityType;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.engine.Rule;
import com.portfolio.weatherintel.engine.RuleResult;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Answers: "Should I carry an umbrella today?"
 *
 * Decision basis: precipitation probability and precipitation amount.
 * Either factor crossing its configured threshold is sufficient to
 * recommend an umbrella - a high probability of light rain and a lower
 * probability of heavy rain are both reasons to carry one, so the two
 * signals are combined with OR rather than requiring both.
 *
 * Verdict semantics for this rule specifically: {@code RECOMMENDED} means
 * "recommended to carry an umbrella", {@code NOT_RECOMMENDED} means "an
 * umbrella is not needed". This differs from the other rules, where the
 * verdict describes suitability of an activity rather than a recommended
 * item to carry - documented here explicitly since it is the one rule
 * where the verdict polarity reads differently from the rest of the engine.
 */
@Component
public class UmbrellaRule implements Rule {

    private final RecommendationRuleProperties.Umbrella thresholds;

    public UmbrellaRule(RecommendationRuleProperties ruleProperties) {
        this.thresholds = ruleProperties.getUmbrella();
    }

    @Override
    public RuleResult evaluate(WeatherSnapshot weather, AirQualitySnapshot airQuality) {
        int probability = weather.precipitationProbabilityPercent();
        double amount = weather.precipitationAmountMm();

        boolean probabilityTriggered = probability >= thresholds.getPrecipitationProbabilityThresholdPercent();
        boolean amountTriggered = amount >= thresholds.getPrecipitationAmountThresholdMm();

        if (probabilityTriggered || amountTriggered) {
            String reasoning = String.format(Locale.US,
                    "%d%% chance of precipitation with %.1fmm expected - carry an umbrella.",
                    probability, amount);
            return new RuleResult(ActivityType.UMBRELLA, RecommendationVerdict.RECOMMENDED, reasoning);
        }

        if (probability >= thresholds.getPrecipitationProbabilityThresholdPercent() / 2) {
            String reasoning = String.format(Locale.US,
                    "%d%% chance of precipitation - low risk, but worth keeping an umbrella nearby.",
                    probability);
            return new RuleResult(ActivityType.UMBRELLA, RecommendationVerdict.CAUTION, reasoning);
        }

        String reasoning = String.format(Locale.US,
                "Only %d%% chance of precipitation expected - an umbrella shouldn't be necessary.",
                probability);
        return new RuleResult(ActivityType.UMBRELLA, RecommendationVerdict.NOT_RECOMMENDED, reasoning);
    }
}
