package com.portfolio.weatherintel.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the JSON response from Open-Meteo's geocoding endpoint
 * (GET /v1/search?name=...), used to resolve a free-text place name
 * typed by the user into coordinates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoGeocodingResponse {

    @JsonProperty("results")
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("name")
        private String name;

        @JsonProperty("latitude")
        private double latitude;

        @JsonProperty("longitude")
        private double longitude;

        @JsonProperty("country")
        private String country;

        @JsonProperty("admin1")
        private String admin1;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getAdmin1() {
            return admin1;
        }

        public void setAdmin1(String admin1) {
            this.admin1 = admin1;
        }

        /**
         * Builds a human-friendly display name such as "Paris, Ile-de-France, France",
         * gracefully omitting parts that are absent.
         */
        public String toDisplayName() {
            StringBuilder sb = new StringBuilder(name);
            if (admin1 != null && !admin1.isBlank() && !admin1.equalsIgnoreCase(name)) {
                sb.append(", ").append(admin1);
            }
            if (country != null && !country.isBlank()) {
                sb.append(", ").append(country);
            }
            return sb.toString();
        }
    }
}
