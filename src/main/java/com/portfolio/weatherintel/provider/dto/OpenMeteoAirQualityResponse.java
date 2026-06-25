package com.portfolio.weatherintel.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the JSON response from Open-Meteo's air quality endpoint
 * (GET /v1/air-quality?current=us_aqi). The {@code us_aqi} field is the
 * US EPA Air Quality Index, already normalized to the 0-500 scale this
 * application's domain model and rules use directly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoAirQualityResponse {

    @JsonProperty("current")
    private CurrentBlock current;

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

        @JsonProperty("us_aqi")
        private Integer usAqi;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Integer getUsAqi() {
            return usAqi;
        }

        public void setUsAqi(Integer usAqi) {
            this.usAqi = usAqi;
        }
    }
}
