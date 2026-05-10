FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY notification-ingest/notification-contracts ./notification-ingest/notification-contracts/
COPY notification-ingest/ingest-api ./notification-ingest/ingest-api/
COPY notification-ingest/notification-router ./notification-ingest/notification-router/
COPY notification-ingest/delivery-worker ./notification-ingest/delivery-worker/
RUN apt-get update \
    && apt-get install -y --no-install-recommends bash ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && chmod +x gradlew \
    && ./gradlew :delivery-worker:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/notification-ingest/delivery-worker/build/libs/delivery-worker-*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
