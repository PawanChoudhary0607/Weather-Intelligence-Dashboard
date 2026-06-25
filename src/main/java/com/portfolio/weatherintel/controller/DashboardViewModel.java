package com.portfolio.weatherintel.controller;

import com.portfolio.weatherintel.domain.WeatherSnapshot;
import com.portfolio.weatherintel.engine.RuleResult;

import java.util.List;

/**
 * The view model assembled by {@link DashboardController} and rendered by
 * the Thymeleaf templates. Keeping this as a single, explicit type (rather
 * than passing loose attributes into the Spring MVC model) makes it clear
 * exactly what data the dashboard view depends on.
 */
public record DashboardViewModel(
        String locationDisplayName,
        WeatherSnapshot weather,
        List<RuleResult> recommendations,
        boolean dataStale,
        String errorMessage
) {

    public static DashboardViewModel withError(String locationDisplayName, String errorMessage) {
        return new DashboardViewModel(locationDisplayName, null, List.of(), false, errorMessage);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
