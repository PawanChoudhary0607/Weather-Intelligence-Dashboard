package com.portfolio.weatherintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for the {@code weather.rules} section of application.yml.
 * Every numeric threshold used by the recommendation engine's rules is defined
 * here so it can be tuned via configuration rather than recompiling code.
 */
@ConfigurationProperties(prefix = "weather.rules")
public class RecommendationRuleProperties {

    private Umbrella umbrella = new Umbrella();
    private Running running = new Running();
    private OutdoorActivity outdoorActivity = new OutdoorActivity();
    private Travel travel = new Travel();
    private AirQuality airQuality = new AirQuality();

    public Umbrella getUmbrella() {
        return umbrella;
    }

    public void setUmbrella(Umbrella umbrella) {
        this.umbrella = umbrella;
    }

    public Running getRunning() {
        return running;
    }

    public void setRunning(Running running) {
        this.running = running;
    }

    public OutdoorActivity getOutdoorActivity() {
        return outdoorActivity;
    }

    public void setOutdoorActivity(OutdoorActivity outdoorActivity) {
        this.outdoorActivity = outdoorActivity;
    }

    public Travel getTravel() {
        return travel;
    }

    public void setTravel(Travel travel) {
        this.travel = travel;
    }

    public AirQuality getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(AirQuality airQuality) {
        this.airQuality = airQuality;
    }

    public static class Umbrella {
        private int precipitationProbabilityThresholdPercent = 50;
        private double precipitationAmountThresholdMm = 1.0;

        public int getPrecipitationProbabilityThresholdPercent() {
            return precipitationProbabilityThresholdPercent;
        }

        public void setPrecipitationProbabilityThresholdPercent(int value) {
            this.precipitationProbabilityThresholdPercent = value;
        }

        public double getPrecipitationAmountThresholdMm() {
            return precipitationAmountThresholdMm;
        }

        public void setPrecipitationAmountThresholdMm(double value) {
            this.precipitationAmountThresholdMm = value;
        }
    }

    public static class Running {
        private double minComfortableTempC = 5.0;
        private double maxComfortableTempC = 28.0;
        private int maxAqi = 100;
        private double maxWindSpeedKmh = 30.0;

        public double getMinComfortableTempC() {
            return minComfortableTempC;
        }

        public void setMinComfortableTempC(double value) {
            this.minComfortableTempC = value;
        }

        public double getMaxComfortableTempC() {
            return maxComfortableTempC;
        }

        public void setMaxComfortableTempC(double value) {
            this.maxComfortableTempC = value;
        }

        public int getMaxAqi() {
            return maxAqi;
        }

        public void setMaxAqi(int value) {
            this.maxAqi = value;
        }

        public double getMaxWindSpeedKmh() {
            return maxWindSpeedKmh;
        }

        public void setMaxWindSpeedKmh(double value) {
            this.maxWindSpeedKmh = value;
        }
    }

    public static class OutdoorActivity {
        private double minComfortableTempC = 0.0;
        private double maxComfortableTempC = 35.0;
        private int maxAqi = 150;
        private int maxPrecipitationProbabilityPercent = 60;

        public double getMinComfortableTempC() {
            return minComfortableTempC;
        }

        public void setMinComfortableTempC(double value) {
            this.minComfortableTempC = value;
        }

        public double getMaxComfortableTempC() {
            return maxComfortableTempC;
        }

        public void setMaxComfortableTempC(double value) {
            this.maxComfortableTempC = value;
        }

        public int getMaxAqi() {
            return maxAqi;
        }

        public void setMaxAqi(int value) {
            this.maxAqi = value;
        }

        public int getMaxPrecipitationProbabilityPercent() {
            return maxPrecipitationProbabilityPercent;
        }

        public void setMaxPrecipitationProbabilityPercent(int value) {
            this.maxPrecipitationProbabilityPercent = value;
        }
    }

    public static class Travel {
        private double maxWindSpeedKmh = 50.0;
        private int maxPrecipitationProbabilityPercent = 70;
        private double heavyPrecipitationAmountMm = 10.0;

        public double getMaxWindSpeedKmh() {
            return maxWindSpeedKmh;
        }

        public void setMaxWindSpeedKmh(double value) {
            this.maxWindSpeedKmh = value;
        }

        public int getMaxPrecipitationProbabilityPercent() {
            return maxPrecipitationProbabilityPercent;
        }

        public void setMaxPrecipitationProbabilityPercent(int value) {
            this.maxPrecipitationProbabilityPercent = value;
        }

        public double getHeavyPrecipitationAmountMm() {
            return heavyPrecipitationAmountMm;
        }

        public void setHeavyPrecipitationAmountMm(double value) {
            this.heavyPrecipitationAmountMm = value;
        }
    }

    public static class AirQuality {
        private int goodMax = 50;
        private int moderateMax = 100;
        private int unhealthySensitiveMax = 150;
        private int unhealthyMax = 200;

        public int getGoodMax() {
            return goodMax;
        }

        public void setGoodMax(int value) {
            this.goodMax = value;
        }

        public int getModerateMax() {
            return moderateMax;
        }

        public void setModerateMax(int value) {
            this.moderateMax = value;
        }

        public int getUnhealthySensitiveMax() {
            return unhealthySensitiveMax;
        }

        public void setUnhealthySensitiveMax(int value) {
            this.unhealthySensitiveMax = value;
        }

        public int getUnhealthyMax() {
            return unhealthyMax;
        }

        public void setUnhealthyMax(int value) {
            this.unhealthyMax = value;
        }
    }
}
