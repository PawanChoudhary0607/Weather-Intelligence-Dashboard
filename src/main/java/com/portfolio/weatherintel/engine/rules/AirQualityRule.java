package com.portfolio.weatherintel.engine.rules;

import com.portfolio.weatherintel.config.RecommendationRuleProperties;
import com.portfolio.weatherintel.domain.ActivityType;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.RecommendationVerdict;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.engine.Rule;
import com.portfolio.weatherintel.engine.RuleResult;
import org.springframework.stereotype.Component;

/**
 * Answers: "Is air quality safe?"
 *
 * Decision basis: pure AQI banding aligned with the US EPA Air Quality
 * Index scale (0-500). This rule does not factor in weather conditions at
 * all - it answers a single, focused question independent of temperature
 * or precipitation, by design, so its reasoning stays simple and directly
 * traceable to one number.
 *
 * If air quality data could not be retrieved, this rule returns a CAUTION
 * verdict explaining the data is unavailable rather than fabricating a
 * value or silently omitting the card.
 */
@Component
public class AirQualityRule implements Rule {

    private final RecommendationRuleProperties.AirQuality thresholds;

    public AirQualityRule(RecommendationRuleProperties ruleProperties) {
        this.thresholds = ruleProperties.getAirQuality();
    }

    @Override
    public RuleResult evaluate(WeatherSnapshot weather, AirQualitySnapshot airQuality) {
        if (airQuality == null) {
            return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.CAUTION,
                    "Air quality data is temporarily unavailable for this location.");
        }

        int aqi = airQuality.aqi();

        if (aqi <= thresholds.getGoodMax()) {
            return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.RECOMMENDED,
                    "AQI is " + aqi + " (Good) - air quality is safe for everyone.");
        }

        if (aqi <= thresholds.getModerateMax()) {
            return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.RECOMMENDED,
                    "AQI is " + aqi + " (Moderate) - acceptable for most people.");
        }

        if (aqi <= thresholds.getUnhealthySensitiveMax()) {
            return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.CAUTION,
                    "AQI is " + aqi + " (Unhealthy for Sensitive Groups) - people with respiratory "
                            + "conditions should limit prolonged outdoor exertion.");
        }

        if (aqi <= thresholds.getUnhealthyMax()) {
            return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.NOT_RECOMMENDED,
                    "AQI is " + aqi + " (Unhealthy) - everyone should reduce prolonged outdoor exertion.");
        }

        return new RuleResult(ActivityType.AIR_QUALITY, RecommendationVerdict.NOT_RECOMMENDED,
                "AQI is " + aqi + " (Very Unhealthy or worse) - avoid outdoor exposure where possible.");
    }
}
