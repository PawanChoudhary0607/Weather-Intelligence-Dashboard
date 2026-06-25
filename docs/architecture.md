# Architecture

## Overview

The Weather Intelligence Dashboard is a single-service Spring Boot application. It takes a
location, fetches normalized weather and air quality data from external providers, runs
that data through a rules-based recommendation engine, and renders the result as a
server-side Thymeleaf page.

```
Browser
   |
   v
DashboardController
   |
   |--> GeocodingService -------> Open-Meteo Geocoding API
   |
   |--> WeatherAggregationService
   |        |
   |        |--> OpenMeteoProvider (primary) -------> Open-Meteo Forecast / Air Quality API
   |        |--> OpenWeatherMapProvider (fallback) --> OpenWeatherMap Current Weather API
   |
   |--> RecommendationEngine
            |--> UmbrellaRule
            |--> RunningSuitabilityRule
            |--> OutdoorActivityRule
            |--> TravelSuitabilityRule
            |--> AirQualityRule
   |
   v
DashboardViewModel --> dashboard.html (Thymeleaf)
```

## Layering

Every request flows strictly through: **Controller -> Service/Aggregation -> Engine/Provider**.
No layer is skipped. In particular:

- Controllers never call a `WeatherProvider` directly - only `WeatherAggregationService`.
- The `RecommendationEngine` and every `Rule` only ever see the normalized domain model
  (`WeatherSnapshot`, `AirQualitySnapshot`) - never a raw provider DTO.
- Provider-specific JSON shapes are confined entirely to the `provider.dto` package.

## Key design patterns

- **Strategy** - `WeatherProvider` is implemented by `OpenMeteoProvider` and
  `OpenWeatherMapProvider`. `Rule` is implemented by all five recommendation rules. Either
  set can be extended by adding a new class with zero changes to existing code.
- **Fallback chain** - `WeatherAggregationService` tries the primary provider, catches
  `ProviderUnavailableException`, and tries the fallback provider before giving up.
- **Adapter** - each provider's `fetch*` method is solely responsible for translating its
  provider-specific DTO into the shared domain model.
- **Configuration-driven thresholds** - `RecommendationRuleProperties` binds every rule's
  numeric thresholds from `application.yml`, so behavior is tunable without recompiling.

## Caching strategy

Three named Caffeine caches, each with a TTL matched to how quickly that category of data
actually changes:

| Cache | TTL | Rationale |
|---|---|---|
| `weatherCache` | 15 minutes | Current conditions change relatively quickly |
| `airQualityCache` | 45 minutes | Air quality changes more slowly than weather |
| `geocodingCache` | 24 hours | A place name's coordinates are effectively static |

## Resilience

Every outbound HTTP call goes through a `RestClient` configured with explicit connect and
read timeouts, so a slow or hanging provider cannot block a request indefinitely. Failures
are caught as `ProviderUnavailableException` and handled at each layer:

1. `WeatherAggregationService` falls back from Open-Meteo to OpenWeatherMap for weather.
2. If both fail, the exception propagates to `DashboardController`, which renders a clear,
   friendly error message instead of a stack trace.
3. Air quality has no fallback provider; if it fails, the dashboard still renders with the
   weather-dependent recommendations and an explicit "data unavailable" message for the
   Air Quality card.

## Recommendation engine

`RecommendationEngine` holds no activity-specific logic - it is injected with every Spring
bean implementing `Rule` and simply evaluates each one against the current snapshot. Adding
a sixth recommendation type (e.g. a future "what to wear" rule) requires writing one new
`Rule` implementation; the engine, controller, and templates require no changes.

Each `Rule` returns a `RuleResult` containing not just a verdict but a plain-English
reasoning string citing the specific factor(s) that drove it - this explainability is the
project's core differentiator from a typical weather-display application.
