package com.portfolio.weatherintel.engine;

import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the recommendation engine: runs every registered {@link Rule}
 * against the current weather and air quality snapshot and collects the
 * results.
 *
 * This class deliberately contains no activity-specific logic. Spring
 * injects every {@code @Component}-annotated {@link Rule} implementation
 * found on the classpath, so adding a new recommendation type requires only
 * writing a new {@link Rule} class - this orchestrator and every caller of
 * it remain unchanged.
 */
@Service
public class RecommendationEngine {

    private final List<Rule> rules;

    public RecommendationEngine(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Evaluates every registered rule against the given conditions.
     *
     * @param weather    the current normalized weather snapshot, required.
     * @param airQuality the current air quality snapshot, or {@code null}
     *                   if it could not be retrieved - individual rules are
     *                   responsible for handling a null value gracefully.
     */
    public List<RuleResult> evaluateAll(WeatherSnapshot weather, AirQualitySnapshot airQuality) {
        return rules.stream()
                .map(rule -> rule.evaluate(weather, airQuality))
                .toList();
    }
}
