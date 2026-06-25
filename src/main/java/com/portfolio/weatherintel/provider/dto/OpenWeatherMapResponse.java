package com.portfolio.weatherintel.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the JSON response from OpenWeatherMap's current weather endpoint
 * (GET /data/2.5/weather?lat=..&lon=..&units=metric). This is the fallback
 * provider's response shape; it is intentionally kept separate from the
 * Open-Meteo DTOs since the two APIs structure their data very differently
 * (units, nesting, naming) despite describing the same real-world
 * conditions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherMapResponse {

    @JsonProperty("main")
    private Main main;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("rain")
    private Rain rain;

    @JsonProperty("weather")
    private List<WeatherDescription> weather;

    @JsonProperty("dt")
    private Long dt;

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public List<WeatherDescription> getWeather() {
        return weather;
    }

    public void setWeather(List<WeatherDescription> weather) {
        this.weather = weather;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        @JsonProperty("temp")
        private double temp;

        @JsonProperty("feels_like")
        private double feelsLike;

        @JsonProperty("humidity")
        private double humidity;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public void setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {

        @JsonProperty("speed")
        private double speed;

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        /**
         * OpenWeatherMap returns wind speed in meters/second when units=metric
         * is requested. Converted to km/h to match this application's
         * normalized domain model.
         */
        public double getSpeedKmh() {
            return speed * 3.6;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rain {

        @JsonProperty("1h")
        private Double oneHour;

        public Double getOneHour() {
            return oneHour;
        }

        public void setOneHour(Double oneHour) {
            this.oneHour = oneHour;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherDescription {

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
