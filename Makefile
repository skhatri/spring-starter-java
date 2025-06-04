PWD := $(shell pwd)
DATE := $(shell date +'%Y-%m-%d'T'%H:%M:%S%z')
COMMIT_HASH := $(shell git rev-parse --short HEAD)

.PHONY: clean build test integration-test run image docker-build docker-up security-scan spotbugs dependency-check release-major release-minor release-patch

# Build tasks
clean:
	@./gradlew clean

build: clean
	@./gradlew build -PcommitHash=$(COMMIT_HASH) -PbuildDate=$(DATE)

# Test tasks
test:
	@./gradlew test

integration-test:
	docker-compose --profile=app up -d
	@./gradlew integration-test:test -Dtags="integration"

# Security tasks
security-scan: spotbugs dependency-check

spotbugs:
	@./gradlew spotbugsMain

dependency-check:
	@./gradlew dependencyCheckAggregate

# Run tasks
run:
	@./gradlew app:runApp

# Docker tasks
image: build
	@docker build --no-cache --build-arg BUILD_DATE=$(DATE) --build-arg COMMIT_HASH=$(COMMIT_HASH) -t spring-starter-java .

docker-build: build
	@docker compose build

docker-up: docker-build
	@docker compose up -d

docker-down:
	@docker compose down

# Utility tasks
format:
	@./gradlew spotlessApply

deps:
	@./gradlew dependencies

update-wrapper:
	@./gradlew wrapper --gradle-version=8.12 --distribution-type=all

help:
	@echo "Available targets:"
	@echo "  clean            - Clean build artifacts"
	@echo "  build            - Build the project"
	@echo "  test             - Run unit tests"
	@echo "  test-integration - Run integration tests"
	@echo "  security-scan    - Run all security scans"
	@echo "  spotbugs         - Run SpotBugs static analysis"
	@echo "  dependency-check - Run OWASP dependency check"
	@echo "  run              - Run the application"
	@echo "  image            - Build Docker image"
	@echo "  docker-build     - Build Docker Compose services"
	@echo "  docker-up        - Start Docker Compose services"
	@echo "  docker-down      - Stop Docker Compose services"
	@echo "  format           - Format code"
	@echo "  deps             - Show project dependencies"
	@echo "  update-wrapper   - Update Gradle wrapper" 
