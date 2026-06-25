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
 * Answers: "Is today good for outdoor activities?"
 *
 * Decision basis: a broader, more tolerant version of
 * {@link RunningSuitabilityRule}'s logic - general outdoor activity
 * (e.g. walking, picnics, errands) tolerates a wider temperature range and
 * higher AQI than running does, and is not sensitive to wind in the same
 * way, so wind is intentionally not a factor here. Precipitation
 * probability is included instead, since rain is the dominant factor in
 * whether general outdoor plans are comfortable.
 */
@Component
public class OutdoorActivityRule implements Rule {

    private final RecommendationRuleProperties.OutdoorActivity thresholds;

    public OutdoorActivityRule(RecommendationRuleProperties ruleProperties) {
        this.thresholds = ruleProperties.getOutdoorActivity();
    }

    @Override
    public RuleResult evaluate(WeatherSnapshot weather, AirQualitySnapshot airQuality) {
        List<String> flags = new ArrayList<>();

        double temp = weather.feelsLikeCelsius();
        if (temp < thresholds.getMinComfortableTempC()) {
            flags.add(String.format(Locale.US, "feels-like temperature is %.1f\u00B0C (cold)", temp));
        } else if (temp > thresholds.getMaxComfortableTempC()) {
            flags.add(String.format(Locale.US, "feels-like temperature is %.1f\u00B0C (hot)", temp));
        }

        int precipProbability = weather.precipitationProbabilityPercent();
        if (precipProbability > thresholds.getMaxPrecipitationProbabilityPercent()) {
            flags.add(String.format(Locale.US, "%d%% chance of precipitation", precipProbability));
        }

        if (airQuality != null && airQuality.aqi() > thresholds.getMaxAqi()) {
            flags.add(String.format(Locale.US, "AQI is %d", airQuality.aqi()));
        }

        if (flags.isEmpty()) {
            String reasoning = String.format(Locale.US,
                    "Pleasant conditions for outdoor activities: %.1f\u00B0C feels-like, %d%% chance of rain%s.",
                    temp, precipProbability, airQuality != null ? ", AQI " + airQuality.aqi() : "");
            return new RuleResult(ActivityType.OUTDOOR_ACTIVITY, RecommendationVerdict.RECOMMENDED, reasoning);
        }

        if (flags.size() == 1) {
            String reasoning = "Still workable outdoors, with caution: " + flags.get(0) + ".";
            return new RuleResult(ActivityType.OUTDOOR_ACTIVITY, RecommendationVerdict.CAUTION, reasoning);
        }

        String reasoning = "Not recommended for outdoor activities: " + String.join(", ", flags) + ".";
        return new RuleResult(ActivityType.OUTDOOR_ACTIVITY, RecommendationVerdict.NOT_RECOMMENDED, reasoning);
    }
}
