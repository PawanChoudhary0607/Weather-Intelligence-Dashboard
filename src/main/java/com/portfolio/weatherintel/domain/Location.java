package com.portfolio.weatherintel.domain;

/**
 * A resolved geographic location used as the input to weather lookups.
 *
 * Latitude/longitude are the canonical identity of a location throughout the
 * system; the display name is purely for showing the user what location
 * their recommendations apply to.
 */
public record Location(double latitude, double longitude, String displayName) {

    public Location {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = "Selected location";
        }
    }

    /**
     * A cache-key-friendly representation that rounds coordinates to roughly
     * 1.1km precision, so that near-identical requests for the same location
     * share a cache entry instead of missing on floating point noise.
     */
    public String toCacheKey() {
        double roundedLat = Math.round(latitude * 100.0) / 100.0;
        double roundedLon = Math.round(longitude * 100.0) / 100.0;
        return roundedLat + "," + roundedLon;
    }
}
