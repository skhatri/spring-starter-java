PWD := $(shell pwd)
DATE := $(shell date +'%Y-%m-%d'T'%H:%M:%S%z')
COMMIT_HASH := $(shell git rev-parse --short HEAD)
TEST_APP ?= poke
TEST_MODULE ?= pokemon

DEP_NAME ?= junit

.PHONY: clean build test integration-test run image dependency compose-build up down security-scan spotbugs dependency-check load-test load-test-basic load-test-high load-test-stress load-test-custom load-test-module release-major release-minor release-patch

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
	@./gradlew integration-test:test -Ptags="integration"

# Load testing tasks

perf-test:
	@echo "🚀 Running Basic Load Test..."
	@./gradlew load-testing:runTest -PappName=$(TEST_APP) -PtestModule=$(TEST_MODULE)

load-test:
	@echo "🔥 Running High Load Test..."
	@./gradlew load-testing:runLoadTest -PappName=$(TEST_APP) -PtestModule=$(TEST_MODULE)

stress-test:
	@echo "💥 Running Stress Test..."
	@./gradlew load-testing:runStressTest -PappName=$(TEST_APP) -PtestModule=$(TEST_MODULE)

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
	@docker build --no-cache --build-arg BUILD_DATE=$(DATE) --build-arg COMMIT_HASH=$(COMMIT_HASH) -t spring-starter-java -f app/Dockerfile.jvm .

compose-build: build
	@docker compose --profile=app build

up: 
	@docker compose --profile=app up -d

down:
	@docker compose --profile="*" down
ps:
	@docker compose ps

# Utility tasks
format:
	@./gradlew spotlessApply

deps:
	@./gradlew dependencies

update-wrapper:
	@./gradlew wrapper --gradle-version=8.12 --distribution-type=all

dependency:
	@./gradlew math-service:dependencyInsight --dependency $(DEP_NAME) --configuration testRuntimeClasspath
	@./gradlew pokemon-service:dependencyInsight --dependency $(DEP_NAME) --configuration testRuntimeClasspath

help:
	@echo "Available targets:"
	@echo "  clean              - Clean build artifacts"
	@echo "  build              - Build the project"
	@echo "  test               - Run unit tests"
	@echo "  integration-test   - Run integration tests"
	@echo "  perf-test          - Run basic Object load test"
	@echo "  load-test          - Run high-load Object test"
	@echo "  stress-test        - Run Object stress test"
	@echo "  security-scan      - Run all security scans"
	@echo "  spotbugs           - Run SpotBugs static analysis"
	@echo "  dependency-check   - Run OWASP dependency check"
	@echo "  run                - Run the application"
	@echo "  image              - Build Docker image"
	@echo "  compose-build      - Build Docker Compose services"
	@echo "  ps                 - List Docker Compose services"
	@echo "  up                 - Start Docker Compose services"
	@echo "  down               - Stop Docker Compose services"
	@echo "  format             - Format code"
	@echo "  deps               - Show project dependencies"
	@echo "  update-wrapper     - Update Gradle wrapper" 
	@echo "  dependency         - Show Gradle Dependency"
