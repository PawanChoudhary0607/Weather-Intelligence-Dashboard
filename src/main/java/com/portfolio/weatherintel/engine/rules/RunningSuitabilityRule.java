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
 * Answers: "Is today good for running?"
 *
 * Decision basis: combines temperature comfort band, air quality, and wind
 * speed. Running is more physiologically sensitive to heat, cold, and poor
 * air quality than general outdoor activity, so this rule uses tighter
 * thresholds than {@link OutdoorActivityRule}. Each factor that falls
 * outside its comfortable range contributes one flag; the final verdict
 * reflects how many flags were triggered rather than any single factor in
 * isolation, since one marginal factor is a caution but multiple compounding
 * factors make running genuinely unsuitable.
 */
@Component
public class RunningSuitabilityRule implements Rule {

    private final RecommendationRuleProperties.Running thresholds;

    public RunningSuitabilityRule(RecommendationRuleProperties ruleProperties) {
        this.thresholds = ruleProperties.getRunning();
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

        double wind = weather.windSpeedKmh();
        if (wind > thresholds.getMaxWindSpeedKmh()) {
            flags.add(String.format(Locale.US, "wind speed is %.0f km/h", wind));
        }

        if (airQuality != null && airQuality.aqi() > thresholds.getMaxAqi()) {
            flags.add(String.format(Locale.US, "AQI is %d", airQuality.aqi()));
        }

        if (flags.isEmpty()) {
            String reasoning = String.format(Locale.US,
                    "Comfortable conditions for running: %.1f\u00B0C feels-like, %.0f km/h wind%s.",
                    temp, wind, airQuality != null ? ", AQI " + airQuality.aqi() : "");
            return new RuleResult(ActivityType.RUNNING, RecommendationVerdict.RECOMMENDED, reasoning);
        }

        if (flags.size() == 1) {
            String reasoning = "Runnable with caution: " + flags.get(0) + ".";
            return new RuleResult(ActivityType.RUNNING, RecommendationVerdict.CAUTION, reasoning);
        }

        String reasoning = "Not recommended for running: " + String.join(", ", flags) + ".";
        return new RuleResult(ActivityType.RUNNING, RecommendationVerdict.NOT_RECOMMENDED, reasoning);
    }
}
