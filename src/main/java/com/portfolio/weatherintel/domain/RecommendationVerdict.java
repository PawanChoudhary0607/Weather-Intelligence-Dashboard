package com.portfolio.weatherintel.domain;

/**
 * The outcome of a single rule's evaluation.
 *
 * RECOMMENDED  - conditions are favorable, no caveats worth surfacing.
 * CAUTION      - conditions are marginal; the activity is still viable but
 *                the user should be aware of a specific factor.
 * NOT_RECOMMENDED - conditions make the activity unsuitable or unsafe.
 */
public enum RecommendationVerdict {
    RECOMMENDED("Recommended", "success"),
    CAUTION("Use Caution", "warning"),
    NOT_RECOMMENDED("Not Recommended", "danger");

    private final String displayLabel;
    private final String severityStyle;

    RecommendationVerdict(String displayLabel, String severityStyle) {
        this.displayLabel = displayLabel;
        this.severityStyle = severityStyle;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * A CSS-friendly style key (success/warning/danger) the Thymeleaf
     * templates use to color-code recommendation cards consistently.
     */
    public String getSeverityStyle() {
        return severityStyle;
    }
}
