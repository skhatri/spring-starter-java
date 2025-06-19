# Integration Testing

This module contains end-to-end tests for the spring-starter-java project using multiple testing frameworks:

- **WireMockTests**: Tests against mock services using WireMock
- **PostgreSQLTests**: Tests against a real PostgreSQL database using Testcontainers
- **KarateTests**: Tests REST API endpoints using Karate framework
- **MathServiceTests**: Tests specific to Math service calculations and GraphQL
- **GraphQLPokemonTests**: Tests GraphQL endpoints for Pokemon service

## Configuration

### Service Host and Port Configuration

The Karate tests support configurable hosts and ports for both Pokemon and Math services:

**Default Configuration:**
- Pokemon Service: `localhost:8080` 
- Math Service: `localhost:8082`

**Configuring Custom Hosts/Ports:**

You can override the default hosts and ports using system properties:

```bash
# Run tests with custom Pokemon service configuration
./gradlew integration-test:test -Dpokemon.host=my-pokemon-host -Dpokemon.port=9090

# Run tests with custom Math service configuration  
./gradlew integration-test:test -Dmath.host=my-math-host -Dmath.port=9091

# Run tests with both services on custom hosts/ports
./gradlew integration-test:test \
    -Dpokemon.host=pokemon.example.com -Dpokemon.port=8080 \
    -Dmath.host=math.example.com -Dmath.port=8082

# For legacy compatibility, server.port still overrides Pokemon service port
./gradlew integration-test:test -Dserver.port=8081
```

**Available Configuration Properties:**
- `pokemon.host` - Pokemon service hostname (default: `localhost`)
- `pokemon.port` - Pokemon service port (default: `8080`)  
- `math.host` - Math service hostname (default: `localhost`)
- `math.port` - Math service port (default: `8082`)
- `server.port` - Legacy property that overrides Pokemon service port

## Running Tests

### Run All Integration Tests

```bash
./gradlew integration-test:test
```

### Run Specific Test Categories

```bash
# Run only Karate tests
./gradlew integration-test:test --tests "*KarateTests*"

# Run only Math service tests
./gradlew integration-test:test --tests "*MathServiceTests*"

# Run only Pokemon GraphQL tests  
./gradlew integration-test:test --tests "*GraphQLPokemonTests*"

# Run with specific tags
./gradlew integration-test:test -Dtags="integration" --tests "KarateTests"
```

### Run Against Different Environments

```bash
# Test against staging environment
./gradlew integration-test:test \
    -Dpokemon.host=pokemon-staging.example.com \
    -Dmath.host=math-staging.example.com

# Test against local development with non-standard ports
./gradlew integration-test:test \
    -Dpokemon.port=8181 \
    -Dmath.port=8282
```

## Test Configuration Details

- **WireMock Tests**: Use `karate-config.js` which defaults to `http://localhost:8080`  
- **Live App Tests**: Use `karate-config.js` which defaults to `http://localhost:8080`
- **Database Tests**: Use embedded PostgreSQL via Testcontainers

The configuration is handled in `karate-config.js` which automatically detects system properties and configures the appropriate service URLs for all Karate feature files. 