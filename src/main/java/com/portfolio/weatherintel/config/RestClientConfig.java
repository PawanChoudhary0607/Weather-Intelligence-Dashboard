package com.portfolio.weatherintel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Builds the {@link RestClient} beans used to call external weather providers.
 *
 * Every outbound HTTP call in this application goes through a client built
 * here, with explicit connect/read timeouts. A slow or hanging third-party
 * API must never be allowed to hang a request thread indefinitely - that is
 * exactly the kind of failure mode the fallback chain in
 * {@code WeatherAggregationService} exists to protect against, but only if
 * the underlying calls actually time out instead of blocking forever.
 */
@Configuration
public class RestClientConfig {

    @Bean(name = "openMeteoRestClient")
    public RestClient openMeteoRestClient(WeatherProviderProperties properties) {
        var openMeteo = properties.getOpenMeteo();
        return RestClient.builder()
                .requestFactory(buildRequestFactory(openMeteo.getConnectTimeoutMs(), openMeteo.getReadTimeoutMs()))
                .build();
    }

    @Bean(name = "openWeatherMapRestClient")
    public RestClient openWeatherMapRestClient(WeatherProviderProperties properties) {
        var openWeatherMap = properties.getOpenWeatherMap();
        return RestClient.builder()
                .requestFactory(buildRequestFactory(openWeatherMap.getConnectTimeoutMs(), openWeatherMap.getReadTimeoutMs()))
                .build();
    }

    private ClientHttpRequestFactory buildRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }
}
