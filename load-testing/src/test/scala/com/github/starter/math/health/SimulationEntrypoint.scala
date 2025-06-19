package com.github.starter.math.health

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

object Health {
  val server = Option(System.getenv("MATH_HOST")).getOrElse("localhost")
  val port = Option(System.getenv("MATH_PORT")).map(_.toInt).getOrElse(8082)
  val ssl = Option(System.getenv("SSL")).map(_.toBoolean).getOrElse(false)
  val http2Enabled = Option(System.getenv("HTTP2")).map(_.toBoolean).getOrElse(true)

  val protocol = if (ssl) "https" else "http"
  val base = s"$protocol://$server:$port"

  private val httpProtocolBase = http
    .baseUrl(base)
    .warmUp(s"$base/actuator/health")
    .doNotTrackHeader("1")
    .acceptHeader("application/json;q=0.9,*/*;q=0.8")
    .contentTypeHeader("application/json")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Gatling-Math-Health-LoadTest/1.0")

  val httpProtocol = if (http2Enabled) {
    httpProtocolBase.enableHttp2.http2PriorKnowledge(Map(s"$server:$port" -> true))
  } else {
    httpProtocolBase
  }

  val actuatorHealthCheck = exec(http("Math Service Actuator Health Check")
    .get("/actuator/health")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
    .check(jsonPath("$.components").exists)
    .check(jsonPath("$.components.ping.status").is("UP"))
    .check(jsonPath("$.components.diskSpace.status").is("UP"))
  )
  .pause(500.milliseconds, 2.seconds)

  val detailedActuatorHealthCheck = exec(http("Math Service Detailed Health Check")
    .get("/actuator/health")
    .queryParam("details", "true")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
    .check(jsonPath("$.components.diskSpace.status").is("UP"))
  )
  .pause(1.second, 3.seconds)

  val metricsCheck = exec(http("Math Service Metrics Check")
    .get("/actuator/metrics")
    .check(status.is(200))
    .check(jsonPath("$.names").exists)
  )
  .pause(500.milliseconds, 2.seconds)

  val prometheusMetricsCheck = exec(http("Math Service Prometheus Metrics")
    .get("/actuator/prometheus")
    .check(status.is(200))
    .check(substring("jvm_memory_used_bytes").exists)
  )
  .pause(500.milliseconds, 2.seconds)

  val mixedHealthChecks = randomSwitch(
    40.0 -> exec(actuatorHealthCheck),
    25.0 -> exec(detailedActuatorHealthCheck),
    20.0 -> exec(metricsCheck),
    15.0 -> exec(prometheusMetricsCheck)
  )

  val rapidHealthCheck = exec(http("Math Service Rapid Health Check")
    .get("/actuator/health")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
  )
  .pause(100.milliseconds, 500.milliseconds)

  val sequentialHealthChecks = exec(actuatorHealthCheck)
    .pause(200.milliseconds)
    .exec(metricsCheck)
    .pause(200.milliseconds)
    .exec(prometheusMetricsCheck)
    .pause(1.second, 2.seconds)
}

class SimulationEntrypoint extends Simulation {

  val actuatorHealthScenario = scenario("Math Service Actuator Health Check")
    .during(2.minutes) {
      exec(Health.actuatorHealthCheck)
    }

  val detailedHealthScenario = scenario("Math Service Detailed Health Check")
    .during(1.minute) {
      exec(Health.detailedActuatorHealthCheck)
    }

  val metricsScenario = scenario("Math Service Metrics Check")
    .during(1.minute) {
      exec(Health.metricsCheck)
    }

  setUp(
    actuatorHealthScenario.inject(rampUsers(5) during (10.seconds)),
    detailedHealthScenario.inject(nothingFor(5.seconds), rampUsers(3) during (5.seconds)),
    metricsScenario.inject(nothingFor(10.seconds), rampUsers(2) during (5.seconds))
  ).protocols(Health.httpProtocol)
   .maxDuration(2.minutes)
}

class LoadSimulationEntrypoint extends Simulation {

  val mixedHealthWorkload = scenario("Math Service Mixed Health Workload")
    .during(3.minutes) {
      exec(Health.mixedHealthChecks)
    }

  val rapidHealthChecks = scenario("Math Service Rapid Health Monitoring")
    .during(2.minutes) {
      exec(Health.rapidHealthCheck)
    }

  setUp(
    mixedHealthWorkload.inject(
      rampUsers(8) during (30.seconds),
      constantUsersPerSec(20) during (1.minute),
      rampUsers(8) during (30.seconds)
    ),
    rapidHealthChecks.inject(
      nothingFor(30.seconds),
      rampUsers(15) during (20.seconds),
      constantUsersPerSec(15) during (1.minute)
    )
  ).protocols(Health.httpProtocol)
   .maxDuration(3.minutes)
}

class StressSimulationEntrypoint extends Simulation {

  val healthStressTest = scenario("Math Service Health Endpoint Stress Test")
    .during(5.minutes) {
      exec(Health.mixedHealthChecks)
    }

  val highFrequencyMonitoring = scenario("Math Service High-Frequency Health Monitoring")
    .during(4.minutes) {
      exec(Health.rapidHealthCheck)
    }

  val sequentialHealthTest = scenario("Math Service Sequential Health Check Test")
    .during(3.minutes) {
      exec(Health.sequentialHealthChecks)
    }

  setUp(
    healthStressTest.inject(
      rampUsers(25) during (1.minute),
      constantUsersPerSec(25) during (2.minutes),
      rampUsers(15) during (1.minute)
    ),
    highFrequencyMonitoring.inject(
      nothingFor(30.seconds),
      rampUsers(30) during (45.seconds),
      constantUsersPerSec(20) during (2.minutes)
    ),
    sequentialHealthTest.inject(
      nothingFor(1.minute),
      rampUsers(10) during (30.seconds),
      constantUsersPerSec(10) during (2.minutes)
    )
  ).protocols(Health.httpProtocol)
   .maxDuration(5.minutes)
   .assertions(
     global.responseTime.max.lt(3000),
     global.responseTime.mean.lt(500),
     global.successfulRequests.percent.gt(99),
     forAll.failedRequests.count.is(0)
   )
} 