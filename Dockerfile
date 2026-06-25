# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build stage: compiles the application with Maven and the full JDK.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

# Copy the Maven wrapper and pom first so dependency resolution is cached
# in its own Docker layer and only re-runs when pom.xml actually changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage: slim JRE-only image, no build tooling included.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/bash appuser
WORKDIR /app

COPY --from=build /workspace/target/weather-intelligence-dashboard.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
