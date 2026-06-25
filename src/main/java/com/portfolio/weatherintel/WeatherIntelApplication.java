package com.portfolio.weatherintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherIntelApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherIntelApplication.class, args);
    }
}
