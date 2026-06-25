package com.portfolio.weatherintel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the full Spring application context loads successfully with all
 * beans wired - configuration properties, both RestClient beans, both
 * providers, the cache manager, the recommendation engine with all five
 * rules, and every controller/service. This is the cheapest possible test
 * that catches wiring mistakes (missing beans, ambiguous autowiring,
 * malformed configuration binding) before they reach a deployed instance.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "OPENWEATHERMAP_API_KEY=test-key-for-context-load"
})
class WeatherIntelApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: a failure to load the context fails this test.
    }
}
