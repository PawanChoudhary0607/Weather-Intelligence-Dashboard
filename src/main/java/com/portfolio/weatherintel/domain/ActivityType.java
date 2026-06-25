package com.portfolio.weatherintel.domain;

/**
 * The set of practical questions the recommendation engine answers.
 * Each {@code Rule} implementation in the engine targets exactly one of
 * these activity types.
 */
public enum ActivityType {
    UMBRELLA("Umbrella"),
    RUNNING("Running"),
    OUTDOOR_ACTIVITY("Outdoor Activity"),
    TRAVEL("Travel"),
    AIR_QUALITY("Air Quality");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
