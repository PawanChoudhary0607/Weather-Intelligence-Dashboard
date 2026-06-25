package com.portfolio.weatherintel.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the JSON response from Open-Meteo's forecast endpoint
 * (GET /v1/forecast?current=temperature_2m,...) when requesting current
 * conditions. Only the fields this application actually uses are declared;
 * unknown properties are ignored rather than causing deserialization
 * failures, since Open-Meteo may add fields over time.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoForecastResponse {

    private double latitude;
    private double longitude;

    @JsonProperty("current")
    private CurrentBlock current;

    @JsonProperty("hourly")
    private HourlyBlock hourly;

    public HourlyBlock getHourly() {
        return hourly;
    }

    public void setHourly(HourlyBlock hourly) {
        this.hourly = hourly;
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

    public CurrentBlock getCurrent() {
        return current;
    }

    public void setCurrent(CurrentBlock current) {
        this.current = current;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentBlock {

        @JsonProperty("time")
        private String time;

        @JsonProperty("temperature_2m")
        private double temperature2m;

        @JsonProperty("apparent_temperature")
        private double apparentTemperature;

        @JsonProperty("relative_humidity_2m")
        private double relativeHumidity2m;

        @JsonProperty("precipitation")
        private double precipitation;

        @JsonProperty("wind_speed_10m")
        private double windSpeed10m;

        @JsonProperty("weather_code")
        private Integer weatherCode;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public double getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(double temperature2m) {
            this.temperature2m = temperature2m;
        }

        public double getApparentTemperature() {
            return apparentTemperature;
        }

        public void setApparentTemperature(double apparentTemperature) {
            this.apparentTemperature = apparentTemperature;
        }

        public double getRelativeHumidity2m() {
            return relativeHumidity2m;
        }

        public void setRelativeHumidity2m(double relativeHumidity2m) {
            this.relativeHumidity2m = relativeHumidity2m;
        }

        public double getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(double precipitation) {
            this.precipitation = precipitation;
        }

        public double getWindSpeed10m() {
            return windSpeed10m;
        }

        public void setWindSpeed10m(double windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }

        public Integer getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(Integer weatherCode) {
            this.weatherCode = weatherCode;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyBlock {

        @JsonProperty("time")
        private java.util.List<String> time;

        @JsonProperty("precipitation_probability")
        private java.util.List<Integer> precipitationProbability;

        public java.util.List<String> getTime() {
            return time;
        }

        public void setTime(java.util.List<String> time) {
            this.time = time;
        }

        public java.util.List<Integer> getPrecipitationProbability() {
            return precipitationProbability;
        }

        public void setPrecipitationProbability(java.util.List<Integer> precipitationProbability) {
            this.precipitationProbability = precipitationProbability;
        }

        /**
         * Returns the precipitation probability for the hour matching the
         * current observation time, falling back to the first available
         * hourly value if an exact match cannot be found. Open-Meteo only
         * exposes precipitation probability on the hourly forecast, not on
         * the current-conditions block, so the current weather's timestamp
         * is matched against this hourly series.
         */
        public int findProbabilityForHour(String currentTimeIso) {
            if (time == null || precipitationProbability == null || time.isEmpty()) {
                return 0;
            }
            String currentHourPrefix = currentTimeIso != null && currentTimeIso.length() >= 13
                    ? currentTimeIso.substring(0, 13)
                    : currentTimeIso;
            for (int i = 0; i < time.size(); i++) {
                String hourPrefix = time.get(i).length() >= 13 ? time.get(i).substring(0, 13) : time.get(i);
                if (hourPrefix.equals(currentHourPrefix)) {
                    Integer value = precipitationProbability.get(i);
                    return value != null ? value : 0;
                }
            }
            Integer first = precipitationProbability.get(0);
            return first != null ? first : 0;
        }
    }
}
