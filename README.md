# 🚀 Spring Boot Pokémon API Starter

> **Production-ready Spring Boot microservice** demonstrating modern Java development practices through a real-world Pokémon API

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[Quick Start](#-quick-start) | [API Examples](#-api-examples) | [Architecture](#️-architecture) | [Testing](#-testing) | [OpenTelemetry](#-observability--opentelemetry)

---

## ⚡ Quick Start (< 2 minutes)

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/) (that's it!)

### 🚀 Launch Everything
```bash
# 1. Clone the repository
git clone https://github.com/username/spring-starter-java.git
cd spring-starter-java

# 2. Start all services (API + Database + Monitoring)
docker-compose --profile=app up -d

# 3. Wait for services to be ready (30-60 seconds)
docker-compose logs -f app | grep "Started Application"

# 4. Test the API
curl "http://localhost:8080/pokemon/list?limit=5" | jq '.'
```

### ✅ Verify Everything Works
```bash
# Health check
curl http://localhost:8080/actuator/health

# Get your first Pokémon
curl http://localhost:8080/pokemon/pikachu | jq '.'

# Try GraphQL (opens in browser)
open http://localhost:8080/graphiql?path=/graphql
```

### 🎯 What's Running
- **🔗 API**: http://localhost:8080 (REST + GraphQL)
- **🗄️ Database**: PostgreSQL with sample data (808 Pokémon)
- **📊 Monitoring**: Grafana (http://localhost:3000), Prometheus, Jaeger

---

## 🎯 What You'll Learn

This project showcases **production-grade Java microservice patterns**:

### 🏗️ Architecture Patterns
- ✅ **Clean Architecture**: Domain-driven design with clear boundaries
- ✅ **Reactive Programming**: Non-blocking I/O with Spring WebFlux & R2DBC
- ✅ **Cursor-based Pagination**: Efficient large dataset handling
- ✅ **API-First Design**: Both REST and GraphQL endpoints

### 🛠️ Modern Java Stack
- ✅ **Java 21**: Latest LTS features (virtual threads, pattern matching)
- ✅ **Spring Boot 3.5**: Native compilation, improved observability
- ✅ **R2DBC**: Reactive database access patterns
- ✅ **PostgreSQL**: Advanced SQL features and JSON support

### 🔍 Production Practices
- ✅ **Comprehensive Testing**: Unit, integration, API, performance tests
- ✅ **Observability**: Metrics, tracing, logging with OpenTelemetry
- ✅ **Security**: Input validation, error handling, vulnerability scanning
- ✅ **DevOps**: Docker, database migrations, CI/CD ready

---

## 🔧 API Examples

### 📋 REST API

#### Get Pokémon List (Paginated)
```bash
# Basic list
curl "http://localhost:8080/pokemon/list?limit=5" | jq '.'

# With sorting
curl "http://localhost:8080/pokemon/list?limit=3&sortField=total&sortDirection=DESC" | jq '.'

# Next page using cursor
curl "http://localhost:8080/pokemon/list?limit=3&nextToken=eyJmaWVsZCI6InRvdGFsIiwidmFsdWUiOjc4MCwiZGlyZWN0aW9uIjoiREVTQyJ9" | jq '.'
```

#### Get Specific Pokémon
```bash
# Single Pokémon with type effectiveness
curl "http://localhost:8080/pokemon/charizard" | jq '.'

# Type effectiveness chart
curl "http://localhost:8080/pokemon/effectiveness/fire%20flying" | jq '.'
```

#### Expected Response Format
```json
{
  "content": [
    {
      "name": "charizard",
      "primaryType": "fire",
      "secondaryType": "flying",
      "total": 634,
      "hp": 78,
      "attack": 84,
      "effectiveness": {
        "superEffective": ["grass", "fighting", "bug", "steel"],
        "notVeryEffective": ["fire", "water", "electric", "rock"]
      }
    }
  ],
  "nextCursor": "eyJmaWVsZCI6...",
  "hasNext": true,
  "totalCount": 808
}
```

### 🎯 GraphQL API

#### Interactive Query Builder
Visit: **http://localhost:8080/graphiql?path=/graphql**

#### Example Queries
```graphql
# Get Pokémon with custom fields
query GetPokemon {
  pokemon(name: "pikachu") {
    name
    primaryType
    total
    effectiveness {
      superEffective
      notVeryEffective
      noEffect
    }
  }
}

# Paginated list with cursor
query GetPokemonList {
  getPokemonList(limit: 5, sortField: "total", sortDirection: "DESC") {
    content {
      name
      total
      primaryType
      secondaryType
    }
    nextCursor
    hasNext
    totalCount
  }
}

# Type effectiveness lookup
query GetEffectiveness {
  effectiveness(typeName: "fire flying") {
    typeName
    superEffective
    effective
    notVeryEffective
    noEffect
  }
}
```

---

## 🏗️ Architecture

### 📐 Clean Architecture Layers
```
┌─ Presentation Layer ──────────────────────┐
│  REST Controllers │ GraphQL Resolvers     │
├─ Application Layer ───────────────────────┤  
│  Use Cases │ Services │ Adapters          │
├─ Domain Layer ────────────────────────────┤
│  Entities │ Value Objects │ Domain Logic  │
├─ Infrastructure Layer ────────────────────┤
│  Database │ External APIs │ Configuration │
└───────────────────────────────────────────┘
```

### 🔄 Request Flow
1. **HTTP/GraphQL Request** → Controller/Resolver
2. **Validation & Mapping** → Application Service  
3. **Business Logic** → Domain Service
4. **Data Access** → Repository (R2DBC)
5. **Response Mapping** → Client

### 📦 Module Structure
```
app/src/main/java/com/github/starter/
├── core/                 # Cross-cutting concerns
│   ├── exception/        # Global error handling
│   └── config/           # Application configuration
├── modules/              # Business modules
│   ├── pokemon/          # Pokémon domain logic
│   │   ├── endpoint/     # REST & GraphQL endpoints
│   │   ├── service/      # Business services
│   │   ├── repository/   # Data access layer
│   │   └── model/        # Domain entities
│   ├── pagination/       # Reusable pagination patterns
│   └── index/            # API documentation
└── Application.java      # Bootstrap application
```

### 🔧 Key Design Patterns
- **Repository Pattern**: Clean data access abstraction
- **Service Layer**: Business logic encapsulation  
- **Adapter Pattern**: External API integration
- **Factory Method**: Object creation strategies
- **Strategy Pattern**: Algorithm selection (pagination, sorting)

---

## 🧪 Testing Strategy

### 🎯 Test Pyramid
```
       /\     E2E Tests (Karate)
      /  \    ← Full system integration
     /____\   
    / Unit  \  Integration Tests (TestContainers)  
   /________\ ← Database + External services
  /   Unit   \
 /____________\ Unit Tests (JUnit 5 + Mockito)
← Fast feedback loop
```

### 🚀 Run Tests
```bash
# Unit tests (fast - 311 tests)
./gradlew test

# Integration tests (with real database)
./gradlew integration-test:test

# All tests with coverage report
./gradlew test jacocoTestReport
open app/build/reports/jacoco/test/html/index.html

# Performance tests
./gradlew test -PincludeTags=performance

# Run specific test categories
./gradlew test -PincludeTags=unit        # Unit tests only
./gradlew test -PincludeTags=integration # Integration tests only
./gradlew test -PincludeTags=api         # API tests only
```

### 📊 Test Categories
- **Unit (95%)**: Business logic, validation, error handling
- **Integration (4%)**: Database operations, external services  
- **API (1%)**: End-to-end request/response testing
- **Performance**: Load testing, response time validation

### 🎯 Test Coverage
- **Line Coverage**: > 80%
- **Branch Coverage**: > 70%
- **Performance Tests**: CI-aware thresholds for reliable builds

---

## 🔍 Observability & OpenTelemetry

### 🎯 Complete Observability Stack
This project implements **production-grade observability** using the **OpenTelemetry standard** with full metrics, traces, and logs integration.

```bash
# Start complete monitoring stack
docker-compose --profile=otel up -d

# Access dashboards
open http://localhost:3000    # Grafana (admin/admin1) - Dashboards & Visualization
open http://localhost:9090    # Prometheus - Metrics Collection
open http://localhost:16686   # Jaeger - Distributed Tracing
```

### 🔧 OpenTelemetry Architecture

```
┌─ Spring Boot App ─────────────────────┐
│  📊 Micrometer Metrics               │
│  🔍 Custom Traces (Pokemon API)      │
│  📝 Structured Logging               │
│         │                            │
│         ▼ OTLP (gRPC)                │
└───────────────────────────────────────┘
         │ :4317
         ▼
┌─ OpenTelemetry Collector ─────────────┐
│  🔄 Receives: Traces, Metrics, Logs  │
│  🎯 Processes: Batching, Filtering    │
│  📤 Exports to: Jaeger, Prometheus   │
└───────────────────────────────────────┘
         │                    │
         ▼ :4317              ▼ :8889
┌─ Jaeger ─────────┐  ┌─ Prometheus ──┐
│  🔍 Trace Storage │  │  📊 Metrics   │
│  🕸️ Service Map   │  │  📈 Time Series│
└───────────────────┘  └───────────────┘
         │                    │
         ▼                    ▼
┌─ Grafana ─────────────────────────────┐
│  📊 Unified Dashboards               │
│  🔍 Trace Correlation               │
│  🚨 Alerting & Monitoring           │
└───────────────────────────────────────┘
```

### 📊 Metrics Collection

#### 🎯 Application Metrics (Micrometer + Spring Boot Actuator)
```bash
# All available metrics
curl http://localhost:8080/actuator/metrics | jq '.names[]' | head -20

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
curl http://localhost:8080/actuator/metrics/jvm.threads.live

# HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# R2DBC database metrics
curl http://localhost:8080/actuator/metrics/r2dbc.pool.acquired
```

#### 📈 Key Performance Indicators
- **🚀 Request Rate**: `http_server_requests_total` - Requests per second
- **⏱️ Response Time**: `http_server_requests_duration` - P50, P95, P99 latencies
- **❌ Error Rate**: `http_server_requests_total{status=~"4..|5.."}` - Error percentage
- **💾 Memory Usage**: `jvm_memory_used_bytes` - Heap and non-heap memory
- **🔗 Database Connections**: `r2dbc_pool_acquired_total` - Connection pool health
- **🎯 Business Metrics**: `pokemon_queries_total`, `effectiveness_lookups_total`

### 🔍 Distributed Tracing

#### 🎯 Custom Pokemon API Tracing
The application includes **custom instrumentation** for Pokemon-specific operations:

```java
// Automatic HTTP request tracing
GET /pokemon/list → Span: "GET /pokemon/list"
  ├── pokemon.list.rest → Custom business logic span
  ├── SELECT app.pokemons → Database query span  
  └── pokemon.enrich_with_effectiveness → Data enrichment span

// GraphQL query tracing  
POST /graphql → Span: "POST /graphql"
  ├── pokemon.graphql.query → GraphQL resolver span
  ├── pokemon.fetch_by_name → Data fetching span
  └── SELECT app.pokemons → Database query span
```

#### 🔍 Trace Attributes & Context
Each trace includes rich contextual information:
- **HTTP Attributes**: Method, URL, status code, user agent
- **Pokemon Attributes**: `pokemon.name`, `pokemon.type`, `pokemon.count`
- **Database Attributes**: Query type, table name, execution time
- **Error Attributes**: Exception type, error message, stack trace

#### 🕸️ Service Map & Dependencies
Jaeger automatically builds a **service dependency map** showing:
- Request flow between components
- Service-to-service communication patterns
- Performance bottlenecks and error rates
- Database query performance

### 📋 Health Checks & Monitoring

#### 🏥 Application Health
```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Detailed health with all components
curl http://localhost:8080/actuator/health | jq '.'
# Returns: database, diskSpace, ping, r2dbc status

# Readiness probe (Kubernetes-ready)
curl http://localhost:8080/actuator/health/readiness

# Liveness probe (Kubernetes-ready)  
curl http://localhost:8080/actuator/health/liveness
```

#### 📊 Prometheus Metrics Endpoint
```bash
# Prometheus-formatted metrics
curl http://localhost:8080/actuator/prometheus

# Sample output:
# http_server_requests_total{method="GET",uri="/pokemon/list",status="200"} 42.0
# http_server_requests_seconds_count{uri="/pokemon/list",status="200"} 50.0
# jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 1.048576E7
# jvm_threads_live_threads 48.0
# jvm_threads_peak_threads 52.0
# jvm_threads_daemon_threads 44.0
# r2dbc_pool_acquired_total{name="connectionFactory"} 15.0
```

### 🎯 Grafana Dashboards

#### 📊 Pre-configured Dashboards
1. **🚀 Spring Boot Overview**: JVM metrics, HTTP requests, error rates, thread monitoring
2. **🗄️ Database Performance**: R2DBC connections, query performance
3. **🎯 Pokemon API Metrics**: Business-specific KPIs and usage patterns
4. **🔍 Distributed Tracing**: Request flow visualization and performance analysis

#### 📈 Key Visualizations
- **Request Rate Timeline**: Requests/second over time with status code breakdown
- **Response Time Heatmap**: P50/P95/P99 latency percentiles
- **JVM Thread Monitoring**: Live, peak, and daemon thread counts over time
- **HTTP Requests by URI**: Bar chart showing request distribution across endpoints
- **Error Rate Alerts**: Automatic alerting when error rate > 5%
- **Memory Usage Trends**: JVM heap usage with GC event correlation
- **Database Connection Health**: Pool utilization and connection lifecycle

### 🚨 Alerting & Monitoring

#### 🔔 Built-in Alerts (Grafana)
- **High Error Rate**: > 5% errors in 5-minute window
- **Slow Response Time**: P95 latency > 500ms for 2 minutes
- **Memory Pressure**: JVM heap usage > 85% for 5 minutes
- **Database Issues**: R2DBC connection failures or timeouts

#### 📱 Alert Channels
Configure notifications via:
- **Slack**: Real-time team notifications
- **Email**: Critical issue escalation
- **PagerDuty**: On-call engineer alerts
- **Webhook**: Custom integrations

### 🔧 OpenTelemetry Configuration

#### 🎯 Environment Variables
```bash
# OpenTelemetry Collector endpoint (used by custom OpenTelemetryConfig)
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317

# Service identification
OTEL_RESOURCE_ATTRIBUTES=service.name=spring-starter-java,service.version=1.0.0

# Sampling configuration (production: 0.1 = 10%)
OTEL_TRACES_SAMPLER=traceidratio
OTEL_TRACES_SAMPLER_ARG=1.0

# Batch export configuration
OTEL_BSP_MAX_EXPORT_BATCH_SIZE=512
OTEL_BSP_EXPORT_TIMEOUT=30s
```

#### 🔧 Custom Configuration
The application includes custom OpenTelemetry configuration in `OpenTelemetryConfig.java`:
- **OTLP Exporter**: Sends traces to collector via gRPC
- **Resource Attributes**: Service name, version, environment
- **Batch Span Processor**: Optimized for production throughput
- **W3C Trace Context**: Standard trace propagation headers

#### 📁 Configuration Files
```bash
# OpenTelemetry Collector configuration
config/otel-collector-config.yaml    # Collector pipeline configuration

# Docker Compose observability stack
docker-compose.yaml                  # Services: otel-collector, jaeger, prometheus, grafana

# Grafana dashboards and data sources
config/grafana/                      # Pre-configured dashboards and provisioning
├── dashboards/spring-boot-dashboard.json
└── provisioning/
    ├── dashboards/dashboards.yaml
    └── datasources/datasources.yaml

# Prometheus configuration
config/prometheus.yml                # Scraping configuration and targets
```

### 🎯 Observability Best Practices

#### ✅ What's Implemented
- **📊 Golden Signals**: Latency, traffic, errors, saturation monitoring
- **🔍 Structured Logging**: JSON logs with correlation IDs
- **📈 SLI/SLO Tracking**: Service Level Indicators and Objectives
- **🚨 Proactive Alerting**: Issues detected before user impact
- **🔗 Trace Correlation**: Link logs, metrics, and traces
- **📱 Dashboard Standardization**: Consistent visualization patterns

#### 🎯 Production Recommendations
- **Sampling**: Use 1-10% trace sampling in production
- **Retention**: Keep traces for 7 days, metrics for 30 days
- **Alerting**: Focus on user-impacting issues, avoid alert fatigue
- **Dashboards**: Create role-specific views (dev, ops, business)
- **Documentation**: Maintain runbooks for common issues

### 🔍 Troubleshooting Observability

#### 🚨 Common Issues
```bash
# Traces not appearing in Jaeger
docker logs otel-collector | grep -i error
docker logs jaeger | grep -i "received spans"

# Metrics not in Prometheus
curl http://localhost:8080/actuator/prometheus | grep pokemon
docker logs prometheus | grep -i error

# Grafana dashboard issues
docker logs grafana | grep -i error
# Check data source configuration at http://localhost:3000/datasources
```

#### 🔧 Debug Commands
```bash
# Verify OpenTelemetry collector is receiving data
docker logs otel-collector --tail 50 | grep -E "(Span|Metric)"

# Check application trace export
docker logs spring-starter-java | grep -i "otel\|trace"

# Test trace generation
curl "http://localhost:8080/pokemon/pikachu" && \
sleep 5 && \
curl "http://localhost:16686/api/traces?service=spring-starter-java&limit=1"
```

---

## 💻 Development Workflow

### 🛠️ Local Development
```bash
# Start database only
docker-compose --profile=db up -d

# Run application locally
./gradlew app:runApp

# Or use Makefile shortcuts
make run              # Run application
make test             # Run all tests
make docker-build     # Build Docker image
make docker-up        # Start full stack
```

### 🔧 Available Commands
```bash
# Code quality
./gradlew spotbugsMain           # Static analysis
./gradlew dependencyCheck       # Security scan
make security-scan              # Run all security checks

# Database
docker-compose logs db-migration # Check migration status
docker-compose exec postgres psql -U postgres -d starter -c "\dt app.*"

# Debugging
docker-compose logs app         # Application logs
docker-compose logs postgres    # Database logs
```

### 📝 Code Style
- **ErrorProne**: Compile-time bug detection
- **SpotBugs**: Static analysis for potential issues
- **JaCoCo**: Code coverage reporting
- **Dependency Check**: Vulnerability scanning

---

## 🚀 Production Deployment

### 🐳 Docker Deployment
```bash
# Build production image
./gradlew build
docker build -t spring-starter-java .

# Run with production profile
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/starter \
  spring-starter-java
```

### 🔧 Environment Configuration
```bash
# Database connection
SPRING_R2DBC_URL=r2dbc:postgresql://host:port/database
SPRING_R2DBC_USERNAME=username
SPRING_R2DBC_PASSWORD=password

# Observability
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
OTEL_RESOURCE_ATTRIBUTES=service.name=spring-starter-java

# Application settings
DATASET_DIR=/app/data
APP_DB=pg
```

### 📊 Production Considerations
- **Resource Limits**: Configure JVM heap settings (`-Xms512m -Xmx512m`)
- **Database Connections**: R2DBC connection pool sizing
- **Monitoring**: OpenTelemetry traces and metrics collection
- **Security**: Input validation, SQL injection prevention

---

## 🛠️ Troubleshooting

### 🔍 Common Issues

#### Database Connection Failed
```bash
# Check if database is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Verify connection manually
docker-compose exec postgres psql -U postgres -d starter -c "SELECT 1;"
```

#### Application Won't Start
```bash
# Check application logs
docker-compose logs app

# Check if ports are available
lsof -i :8080
lsof -i :5432

# Verify Docker resources
docker system df
```

#### Tests Failing in CI
```bash
# Performance tests are CI-aware
CI=true ./gradlew test

# Check SpotBugs dependency order
./gradlew spotbugsTest --dry-run
```

### 📋 Debug Commands
```bash
# Application health and metrics
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Database connectivity
docker-compose exec postgres pg_isready -U postgres

# Container resource usage
docker stats postgres spring-starter-java

# Network connectivity
docker-compose exec app wget -q --spider http://postgres:5432
```

### 🆘 Getting Help
- **Issues**: [GitHub Issues](https://github.com/skhatri/spring-starter-java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/skhatri/spring-starter-java/discussions)
- **Documentation**: Check the `/docs` directory for additional guides

---

## 🤝 Contributing

### 🔄 Development Process
1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Write tests** for your changes
4. **Run quality checks** (`make security-scan`)
5. **Commit** your changes (`git commit -m 'Add amazing feature'`)
6. **Push** to the branch (`git push origin feature/amazing-feature`)
7. **Open** a Pull Request

### 📋 Code Standards
- **Test Coverage**: Maintain > 80% line coverage
- **Performance**: Tests must pass CI performance thresholds
- **Security**: All dependencies must pass vulnerability scans
- **Documentation**: Update README for new features

### 🎯 Learning Paths
- **Beginner**: Start with REST endpoints and basic CRUD operations
- **Intermediate**: Explore GraphQL, reactive streams, and pagination
- **Advanced**: Dive into observability, performance optimization, and architectural patterns

---

## 📚 Related Projects & Resources

### 🔗 Learning Resources
- **Spring Boot**: [Official Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- **R2DBC**: [Reactive Database Connectivity](https://r2dbc.io/)
- **GraphQL**: [Spring GraphQL Documentation](https://docs.spring.io/spring-graphql/docs/current/reference/html/)
- **Clean Architecture**: [Robert C. Martin's Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)


---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🌟 Acknowledgments

- **Pokémon Data**: Kaggle Pokemon dataset 
- **Spring Team**: For Spring Boot and WebFlux
- **TestContainers**: For integration testing with containers
- **OpenTelemetry**: For observability standards and modules

---

*Built with ❤️ for developers using modern Java microservices*


