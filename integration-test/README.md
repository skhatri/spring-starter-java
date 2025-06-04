# Integration Tests

This module contains integration tests that run against live application instances or mock services.
All tests are tagged with `@Tag("integration")` for proper categorization.

**⚠️ Important: Integration tests are disabled by default and must be explicitly enabled.**

## Test Types

### Live Application Tests
These tests run against a live application instance:

- **GraphQLPokemonTests**: Tests GraphQL Pokemon queries
- **KarateTests**: Tests REST API endpoints using Karate framework

#### Prerequisites for Live App Tests

1. Start the application with PostgreSQL using docker-compose:
   ```bash
   # From the root directory
   docker-compose up -d
   ```

2. Ensure the application is running on `http://localhost:8080`

### Mock Service Tests
These tests run against WireMock containerized services:

- **WireMockTests**: Tests REST API endpoints using WireMock with containerized mock services

#### Prerequisites for Mock Tests

- Docker must be available (WireMock runs in testcontainers)
- No external application needed - WireMock provides the mock server

## Running the Tests

### Run integration tests (explicitly enabled):

```bash
# Using the dedicated integrationTest task (always runs integration tests)
./gradlew integration-test:integrationTest

# Using the standard test task with explicit tag specification
./gradlew integration-test:test -Dtags="integration"
./gradlew integration-test:test -DincludeTags="integration"
```

### Default behavior (no tests run):

```bash
# This will NOT run any tests (integration tests are excluded by default)
./gradlew integration-test:test
```

### Run specific test types:

```bash
# Individual test classes (must specify integration tag)
./gradlew integration-test:test -Dtags="integration" --tests "GraphQLPokemonTests"
./gradlew integration-test:test -Dtags="integration" --tests "KarateTests"
./gradlew integration-test:test -Dtags="integration" --tests "WireMockTests"
```

### Tag filtering examples:

```bash
# Run only integration tests
./gradlew integration-test:test -Dtags="integration"

# Exclude specific tests while running integration tests
./gradlew integration-test:test -Dtags="integration" --exclude-tags="slow"
```

## Configuration

- **Live App Tests**: Use `karate-config.js` which defaults to `http://localhost:8080`
- **Mock Tests**: WireMock starts on random port (40000-40099 range)
- **Mock Data**: Located in `src/test/resources/mock/` directory
- **Test Tagging**: All tests are tagged with `@Tag("integration")`
- **Default Behavior**: Tests are disabled unless explicitly enabled with tags 