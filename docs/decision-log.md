# Decision Log

Short, dated entries explaining the judgment calls behind this project's architecture.
The goal of this file is to make engineering decisions visible, not just their outcome.

---

### Open-Meteo as the primary provider, OpenWeatherMap as fallback only

Open-Meteo requires no API key for current weather, hourly precipitation probability,
air quality, or geocoding. Using it as the primary provider means anyone cloning this
repository can run it immediately with zero setup friction. OpenWeatherMap is kept as a
documented fallback specifically to demonstrate multi-provider resilience design, not
because its data is preferred.

### No provider reconciliation or averaging

Blending two providers' forecasts (e.g. averaging precipitation probability) produces a
number that cannot be cleanly attributed to either source, and undermines the
explainability goal of the recommendation engine - every verdict should trace back to one
citable data source. Exactly one provider answers any given request; the other is used
only on failure.

### No Redis, no database, no authentication in v1

The MVP is intentionally stateless: every request re-fetches and re-evaluates fresh data,
nothing is persisted, and there are no user accounts. This is a deliberate scope
boundary, not an oversight - introducing Redis is the right move once there's a real
caching-coherence reason (multi-instance deployment) and introducing a database/auth is
the right move once there's a real reason to persist anything per-user (e.g. saved
locations or personalization profiles), both of which are out of scope for this MVP.

### Five rules, not a generic weighted-scoring framework

Each rule (umbrella, running, outdoor activity, travel, air quality) is its own class
implementing a shared `Rule` interface, with its own thresholds and its own plain-English
reasoning. A more general scoring/weighting abstraction was considered and deliberately
rejected for v1: with five independent, single-purpose rules, that abstraction would be
solving a problem that doesn't exist yet. It's a natural extraction point for v2 if a rule
needs to combine many weighted factors into one confidence score.

### Caffeine, not Redis, for caching

A single-instance deployment has no cache-coherence problem to solve, so an external cache
server adds operational complexity without a corresponding benefit. Three named in-memory
caches (weather, air quality, geocoding) use different TTLs reflecting how quickly each
category of data actually changes.

### Thresholds are configuration, not constants

Every numeric threshold used by the five rules (temperature comfort bands, AQI breakpoints,
wind speed limits, precipitation thresholds) is bound from `application.yml` via
`@ConfigurationProperties` rather than hardcoded in the rule classes. This means tuning the
engine's behavior - e.g. deciding that running becomes unsuitable at AQI 90 instead of 100 -
is a configuration change, not a code change.

### OpenWeatherMap's free tier cannot supply a true precipitation probability

OpenWeatherMap's free current-weather endpoint has no forecast probability field (that
requires a paid plan). Rather than silently fabricating a number, the fallback provider
approximates probability as 100% when precipitation is currently being reported and 0%
otherwise, and this approximation is documented directly in the provider's Javadoc. This
also means the dashboard surfaces a visible "fallback data source" notice whenever
OpenWeatherMap is the data source in use, instead of presenting fallback data as
equivalent in quality to the primary source.

### OpenWeatherMap fallback does not cover air quality

OpenWeatherMap's free tier has no equivalent to Open-Meteo's air quality endpoint without
a separate, more complex integration (their Air Pollution API). Rather than scope-creeping
the fallback path, air quality has no fallback in the MVP: if Open-Meteo cannot supply it,
the Air Quality card explains that data is temporarily unavailable rather than guessing.
