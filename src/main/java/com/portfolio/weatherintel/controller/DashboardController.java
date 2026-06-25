package com.portfolio.weatherintel.controller;

import com.portfolio.weatherintel.aggregation.WeatherAggregationService;
import com.portfolio.weatherintel.domain.AirQualitySnapshot;
import com.portfolio.weatherintel.domain.Location;
import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.engine.RecommendationEngine;
import com.portfolio.weatherintel.engine.RuleResult;
import com.portfolio.weatherintel.exception.LocationNotFoundException;
import com.portfolio.weatherintel.exception.ProviderUnavailableException;
import com.portfolio.weatherintel.service.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Serves the Weather Intelligence Dashboard's main page.
 *
 * This controller intentionally contains no weather logic, no provider
 * knowledge, and no recommendation logic of its own - it only orchestrates
 * calls to {@link GeocodingService}, {@link WeatherAggregationService}, and
 * {@link RecommendationEngine}, then assembles the result into a
 * {@link DashboardViewModel} for the view layer. Every failure path returns
 * a normal HTML page with a clear, friendly message rather than an
 * unhandled exception page, since a live demo failing ungracefully on a
 * recruiter's first click is the worst possible outcome for a portfolio
 * project.
 */
@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final double DEFAULT_LATITUDE = 51.5074;
    private static final double DEFAULT_LONGITUDE = -0.1278;
    private static final String DEFAULT_LOCATION_NAME = "London, United Kingdom";

    private final GeocodingService geocodingService;
    private final WeatherAggregationService weatherAggregationService;
    private final RecommendationEngine recommendationEngine;

    public DashboardController(GeocodingService geocodingService,
                                WeatherAggregationService weatherAggregationService,
                                RecommendationEngine recommendationEngine) {
        this.geocodingService = geocodingService;
        this.weatherAggregationService = weatherAggregationService;
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "place", required = false) String place,
                             @RequestParam(value = "lat", required = false) Double lat,
                             @RequestParam(value = "lon", required = false) Double lon,
                             Model model) {

        Location location = resolveLocation(place, lat, lon);
        model.addAttribute("viewModel", buildViewModel(location));
        model.addAttribute("searchedPlace", place == null ? "" : place);
        return "dashboard";
    }

    private Location resolveLocation(String place, Double lat, Double lon) {
        try {
            if (lat != null && lon != null) {
                return new Location(lat, lon, "Your location");
            }
            if (place != null && !place.isBlank()) {
                return geocodingService.resolve(place);
            }
        } catch (LocationNotFoundException | IllegalArgumentException ex) {
            log.info("Falling back to default location after resolution failure: {}", ex.getMessage());
        }
        return new Location(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_LOCATION_NAME);
    }

    private DashboardViewModel buildViewModel(Location location) {
        WeatherSnapshot weather;
        try {
            weather = weatherAggregationService.getCurrentWeather(location);
        } catch (ProviderUnavailableException ex) {
            log.error("Weather lookup failed for {}: {}", location.toCacheKey(), ex.getMessage());
            return DashboardViewModel.withError(location.displayName(),
                    "We couldn't retrieve weather data for this location right now. Please try again shortly.");
        }

        AirQualitySnapshot airQuality = null;
        try {
            airQuality = weatherAggregationService.getAirQuality(location);
        } catch (ProviderUnavailableException ex) {
            log.warn("Air quality lookup failed for {}, continuing without it: {}", location.toCacheKey(), ex.getMessage());
        }

        List<RuleResult> recommendations = recommendationEngine.evaluateAll(weather, airQuality);
        boolean stale = !"Open-Meteo".equals(weather.sourceProvider());

        return new DashboardViewModel(location.displayName(), weather, recommendations, stale, null);
    }
}
