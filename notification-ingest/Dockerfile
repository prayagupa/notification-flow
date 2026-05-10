# Build requires Maven 3.6.3+ and JDK 22+ (target JDK 25 per SDS when available)
FROM eclipse-temurin:22-jdk-jammy AS build
WORKDIR /build
COPY pom.xml .
COPY notification-contracts notification-contracts/
COPY ingest-api ingest-api/
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*
RUN mvn -q -pl ingest-api -am package -DskipTests

FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /build/ingest-api/target/ingest-api-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
