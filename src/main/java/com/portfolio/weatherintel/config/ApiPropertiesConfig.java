package com.portfolio.weatherintel.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the typed {@code @ConfigurationProperties} classes as Spring beans.
 * Centralizing this enables-list in one place makes it obvious, at a glance,
 * which configuration trees the application binds to.
 */
@Configuration
@EnableConfigurationProperties({
        WeatherProviderProperties.class,
        RecommendationRuleProperties.class,
        CacheTtlProperties.class
})
public class ApiPropertiesConfig {
}
