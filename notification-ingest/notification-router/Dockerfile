FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY notification-ingest/notification-contracts ./notification-ingest/notification-contracts/
COPY notification-ingest/ingest-api ./notification-ingest/ingest-api/
COPY notification-ingest/notification-router ./notification-ingest/notification-router/
RUN apt-get update \
    && apt-get install -y --no-install-recommends bash ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && chmod +x gradlew \
    && ./gradlew :notification-router:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/notification-ingest/notification-router/build/libs/notification-router-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
