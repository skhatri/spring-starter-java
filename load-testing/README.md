# 🚀 Modular API Load Testing Framework

This module provides a **configurable Gatling load testing framework** that can test any API module using reusable simulation patterns and parameterized configurations.

## 🏗️ **Architecture Overview**

### **Modular Design**
- **Module-Agnostic**: Configure any module via `testModule` property
- **Reusable Tasks**: Generic task names (`runTest`, `runLoadTest`, `runStressTest`) 
- **Standardized Entry Points**: `SimulationEntrypoint.scala` pattern for each module
- **Dynamic Class Loading**: Build system constructs simulation class names automatically

### **Current Module Support**
- ✅ **Pokemon Module** (`testModule=pokemon`)
- 🔄 **Extensible** for future modules (user, product, etc.)

## 🎯 **Test Scenarios**

### **Pokemon Module** (`/pokemon/*`)
Testing the Pokemon API endpoints with comprehensive scenarios:

#### **SimulationEntrypoint** (Basic Load Test)
- **Duration**: 2 minutes
- **Scenarios**: 
  - **Find Pokemon by Name** (10 users ramped over 15s)
  - **List First Page Only** (8 users ramped over 10s)  
  - **Full Pagination** (3 users ramped over 5s)
- **Data Source**: `find-pokemon.csv` with 50+ Pokemon names

#### **LoadSimulationEntrypoint** (High Load Test)
- **Duration**: 3 minutes
- **Scenarios**: Mixed workload with variable concurrency
  - **Ramp-up**: 5-15 concurrent users over 30s
  - **Sustained**: 15 concurrent users for 1 minute
  - **Ramp-down**: 15-5 concurrent users over 30s

#### **StressSimulationEntrypoint** (Stress Test)  
- **Duration**: 5 minutes
- **Load**: 1-30 concurrent users
- **Assertions**:
  - Max response time < 5000ms
  - Mean response time < 1000ms
  - Success rate > 95%

## 🏃‍♂️ **Quick Start**

### **Prerequisites**
```bash
# Start the API server
make docker-up

# Verify API health
curl http://localhost:8080/actuator/health
```

### **Run Load Tests**
```bash
# Basic load test (current module: pokemon)
make load-test

# High load test  
make load-test-high

# Stress test
make load-test-stress

# Test different module (when available)
make load-test TEST_MODULE=user
```

### **Direct Gradle Commands**
```bash
# Basic simulation
./gradlew load-testing:runTest

# Load simulation
./gradlew load-testing:runLoadTest

# Stress simulation
./gradlew load-testing:runStressTest

# Override module
./gradlew load-testing:runTest -PtestModule=pokemon
```

## 📊 **Test Behaviors**

### **Find Pokemon Scenario**
Uses `find-pokemon.csv` feeder with Pokemon names:
```csv
Bulbasaur
Pikachu
Charizard
Mewtwo
...
```

**Request Pattern**:
```http
GET /pokemon/{pokemonName}
```
- **Validation**: Status 200 + name match
- **Pause**: 1-3 seconds between requests

### **List Pokemon Scenarios**

#### **First Page Only**
```http
GET /pokemon/list?limit=10
```
- Quick pagination test
- Validates JSON structure (`$.data`, `$.pagination`)

#### **Full Pagination** 
```http
GET /pokemon/list?limit=20
GET /pokemon/list?limit=20&nextToken={cursor}
```
- Continues until `hasNext=false`
- Tests cursor-based pagination flow
- Simulates complete dataset browsing

### **Mixed Behavior** (Load/Stress Tests)
Random distribution:
- **50%** - Find specific Pokemon
- **50%** - List first page only

## 🔧 **Configuration**

### **Module Configuration**
```properties
# load-testing/gradle.properties
testModule=pokemon
```

### **Environment Variables**
```bash
# Target server (default: localhost)
export HOST=api.example.com

# Target port (default: 8080) 
export PORT=8080

# Enable SSL (default: false)
export SSL=true

# Enable HTTP/2 (default: true)
export HTTP2=true
```

### **Custom Test Execution**
```bash
# Against production
HOST=prod.api.com PORT=443 SSL=true make load-test

# Against staging with different module
HOST=staging.api.com make load-test TEST_MODULE=user

# High load against specific environment
HOST=load-test.api.com make load-test-stress
```

## 📈 **Results & Reports**

### **Report Locations**
```
load-testing/build/reports/gatling/
├── simulationentrypoint-{timestamp}/
│   ├── simulation.log      # Raw execution data
│   └── (no HTML reports - disabled to avoid commercial dependency)
```

### **Console Output Analysis**
Real-time metrics displayed during execution:
- **Total Requests**: Count and success rate
- **Response Times**: Per-endpoint timing  
- **Scenario Progress**: User completion status
- **Active Users**: Current load distribution

### **Sample Results**
```
---- Requests ---------------------|---Total---|-----OK----|----KO----
> Global                          |     1,290 |     1,290 |         0
> Find Pokemon - Pikachu          |        10 |        10 |         0  
> List Pokemon - First Page       |       615 |       615 |         0
> List Pokemon - Next Page        |       116 |       116 |         0
```

## 🎯 **Performance Expectations**

### **Target Response Times** (Pokemon Module)
- **Find Pokemon**: < 150ms (P95)
- **List Pokemon**: < 300ms (P95) 
- **Pagination**: < 200ms (P95)

### **Expected Throughput**
- **Basic Load**: 200+ requests/sec
- **High Load**: 300+ requests/sec  
- **Stress Test**: Sustained load with < 5% failures

### **Resource Usage**
- **JVM Heap**: 512MB-1GB (configured in build.gradle.kts)
- **CPU**: 2-4 cores recommended
- **Network**: Minimal bandwidth

## 🔍 **Troubleshooting**

### **Common Issues**

#### **Connection Refused**
```bash
# Ensure API is running
curl http://localhost:8080/actuator/health

# Check Docker containers  
docker-compose ps
```

#### **Out of Memory**
```bash
# Increase JVM heap in createGatlingTask function
jvmArgs = listOf("-Xms1024m", "-Xmx2048m")
```

#### **CSV Data Issues**
```bash
# Verify CSV file exists and has proper format
cat load-testing/src/test/resources/find-pokemon.csv

# Ensure Pokemon names match API data exactly (case-sensitive)
curl http://localhost:8080/pokemon/Pikachu  # ✅ Works
curl http://localhost:8080/pokemon/pikachu  # ❌ 404
```

#### **Module Configuration**
```bash
# Check current module
grep testModule load-testing/gradle.properties

# Verify simulation class exists
ls load-testing/src/test/scala/com/github/starter/pokemon/
```

## 🚀 **Extending to New Modules**

### **Adding a New Module** (e.g., `user`)

1. **Update Configuration**:
   ```properties
   # load-testing/gradle.properties
   testModule=user
   ```

2. **Create Simulation Package**:
   ```
   load-testing/src/test/scala/com/github/starter/user/
   └── SimulationEntrypoint.scala
   ```

3. **Implement Simulation Classes**:
   ```scala
   package com.github.starter.user
   
   class SimulationEntrypoint extends Simulation { /* basic scenarios */ }
   class LoadSimulationEntrypoint extends Simulation { /* load scenarios */ }  
   class StressSimulationEntrypoint extends Simulation { /* stress scenarios */ }
   ```

4. **Add Test Data**:
   ```
   load-testing/src/test/resources/
   └── find-user.csv  # or appropriate data file
   ```

5. **Run Tests**:
   ```bash
   make load-test TEST_MODULE=user
   ./gradlew load-testing:runTest -PtestModule=user
   ```

### **Build System Benefits**
- **Automatic Class Resolution**: `com.github.starter.${testModule}.SimulationEntrypoint`
- **Reusable Task Names**: Same commands work for any module
- **Parameterized Execution**: Override module via command line
- **Consistent Structure**: Standardized simulation patterns

## 📚 **Technical Details**

### **Build Configuration**
The `build.gradle.kts` uses a reusable `createGatlingTask` function:

```kotlin
val testModule = project.findProperty("testModule") ?: "pokemon"
val basePackage = "com.github.starter.$testModule"

val gatlingSimulations = mapOf(
    "runTest" to "${basePackage}.SimulationEntrypoint",
    "runLoadTest" to "${basePackage}.LoadSimulationEntrypoint", 
    "runStressTest" to "${basePackage}.StressSimulationEntrypoint"
)
```

### **Simulation Architecture**
Each module follows the pattern:
- **Domain Object**: Contains reusable HTTP scenarios
- **SimulationEntrypoint**: Basic load test setup
- **LoadSimulationEntrypoint**: High-load scenarios  
- **StressSimulationEntrypoint**: Stress testing with assertions

### **No Report Generation**
HTML reports are disabled (`-nr` flag) to avoid dependency on commercial Gatling features. All metrics are available via console output and simulation.log files.

## 📚 **Resources**

- [Gatling Documentation](https://gatling.io/docs/)
- [Scala Load Testing Patterns](https://gatling.io/docs/gatling/tutorials/advanced-tutorial/)
- [API Documentation](../README.md)

---

*This modular load testing framework provides consistent, reusable patterns for testing any API module while maintaining flexibility and ease of extension.* 