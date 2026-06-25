package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.ActivityType;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.engine.Rule;
import com.portfolio.weatherintel.engine.RuleResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Answers: "Is today good for travelling?"
 *
 * Decision basis: unlike the comfort-oriented rules, this rule is oriented
 * toward travel disruption and safety - strong wind and heavy or
 * high-probability precipitation are the dominant factors that delay
 * flights, make roads hazardous, or disrupt outdoor transit. Temperature
 * and air quality are deliberately not factored in here, since they affect
 * comfort rather than whether travel itself is safe or likely to be
 * disrupted.
 */
@Component
public class TravelSuitabilityRule implements Rule {

    private final RecommendationRuleProperties.Travel thresholds;

    public TravelSuitabilityRule(RecommendationRuleProperties ruleProperties) {
        this.thresholds = ruleProperties.getTravel();
    }

    @Override
    public RuleResult evaluate(WeatherSnapshot weather, AirQualitySnapshot airQuality) {
        List<String> flags = new ArrayList<>();

        double wind = weather.windSpeedKmh();
        if (wind > thresholds.getMaxWindSpeedKmh()) {
            flags.add(String.format(Locale.US, "wind speed is %.0f km/h", wind));
        }

        int precipProbability = weather.precipitationProbabilityPercent();
        double precipAmount = weather.precipitationAmountMm();
        boolean heavyRain = precipAmount >= thresholds.getHeavyPrecipitationAmountMm();
        boolean highProbability = precipProbability > thresholds.getMaxPrecipitationProbabilityPercent();

        if (heavyRain) {
            flags.add(String.format(Locale.US, "heavy precipitation expected (%.1fmm)", precipAmount));
        } else if (highProbability) {
            flags.add(String.format(Locale.US, "%d%% chance of precipitation", precipProbability));
        }

        if (flags.isEmpty()) {
            String reasoning = String.format(Locale.US,
                    "Favorable travel conditions: %.0f km/h wind, %d%% chance of precipitation.",
                    wind, precipProbability);
            return new RuleResult(ActivityType.TRAVEL, RecommendationVerdict.RECOMMENDED, reasoning);
        }

        if (flags.size() == 1 && !heavyRain) {
            String reasoning = "Travel is possible, but plan for delays: " + flags.get(0) + ".";
            return new RuleResult(ActivityType.TRAVEL, RecommendationVerdict.CAUTION, reasoning);
        }

        String reasoning = "Not recommended for travel: " + String.join(", ", flags) + ".";
        return new RuleResult(ActivityType.TRAVEL, RecommendationVerdict.NOT_RECOMMENDED, reasoning);
    }
}
