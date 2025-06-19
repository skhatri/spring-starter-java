FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .

COPY pokemon-service/build.gradle.kts pokemon-service/
COPY math-service/build.gradle.kts math-service/
COPY starter-platform starter-platform

RUN ./gradlew :pokemon-service:dependencies --no-daemon

COPY pokemon-service/src pokemon-service/src
COPY config config
COPY scripts scripts
COPY db-migration db-migration

RUN ./gradlew :pokemon-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/app/pokemon-service/build/libs/pokemon-service-*.jar app.jar
COPY --from=build /workspace/app/scripts/containers/postgres /app/db

ENV DATASET_DIR=/app/db
ENV APP_DB=pg

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "app.jar"] 