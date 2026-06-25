package com.portfolio.weatherintel.engine;

import com.portfolio.weatherintel.domain.ActivityType;
import com.portfolio.weatherintel.domain.RecommendationVerdict;

/**
 * The result of evaluating a single {@link Rule} against current
 * conditions.
 *
 * {@code reasoning} is a plain-English explanation citing the specific
 * factor(s) that drove the verdict (e.g. "70% chance of rain, 4.2mm
 * expected"). This field is what makes the engine's output explainable
 * rather than a black-box yes/no - every verdict shown on the dashboard
 * traces back to one of these reasoning strings.
 */
public record RuleResult(
        ActivityType activityType,
        RecommendationVerdict verdict,
        String reasoning
) {
}
