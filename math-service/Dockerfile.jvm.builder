FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .

COPY math-service/build.gradle.kts math-service/
COPY pokemon-service/build.gradle.kts pokemon-service/
COPY starter-platform starter-platform
COPY config config

RUN ./gradlew :math-service:dependencies --no-daemon

COPY math-service/src math-service/src

RUN ./gradlew :math-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/app/math-service/build/libs/math-service-*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"] 