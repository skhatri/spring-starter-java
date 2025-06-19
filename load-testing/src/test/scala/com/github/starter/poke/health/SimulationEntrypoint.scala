package com.github.starter.poke.health

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

object Health {
  val server = Option(System.getenv("HOST")).getOrElse("localhost")
  val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
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
    .userAgentHeader("Gatling-Health-LoadTest/1.0")

  val httpProtocol = if (http2Enabled) {
    httpProtocolBase.enableHttp2.http2PriorKnowledge(Map(s"$server:$port" -> true))
  } else {
    httpProtocolBase
  }

  val basicHealthCheck = exec(http("Basic Health Check")
    .get("/health")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
    .check(jsonPath("$.server_time").exists)
  )
  .pause(500.milliseconds, 2.seconds)

  val actuatorHealthCheck = exec(http("Actuator Health Check")
    .get("/actuator/health")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
    .check(jsonPath("$.components").exists)
    .check(jsonPath("$.components.ping.status").is("UP"))
  )
  .pause(500.milliseconds, 2.seconds)

  val detailedActuatorHealthCheck = exec(http("Detailed Actuator Health Check")
    .get("/actuator/health")
    .queryParam("details", "true")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
    .check(jsonPath("$.components.diskSpace.status").is("UP"))
    .check(jsonPath("$.components.r2dbc.status").is("UP"))
  )
  .pause(1.second, 3.seconds)

  val mixedHealthChecks = randomSwitch(
    40.0 -> exec(basicHealthCheck),
    35.0 -> exec(actuatorHealthCheck),
    25.0 -> exec(detailedActuatorHealthCheck)
  )

  val rapidHealthCheck = exec(http("Rapid Health Check")
    .get("/health")
    .check(status.is(200))
    .check(jsonPath("$.status").is("UP"))
  )
  .pause(100.milliseconds, 500.milliseconds)

  val sequentialHealthChecks = exec(basicHealthCheck)
    .pause(200.milliseconds)
    .exec(actuatorHealthCheck)
    .pause(200.milliseconds)
    .exec(detailedActuatorHealthCheck)
    .pause(1.second, 2.seconds)
}

class SimulationEntrypoint extends Simulation {

  val basicHealthScenario = scenario("Basic Health Check")
    .during(2.minutes) {
      exec(Health.basicHealthCheck)
    }

  val actuatorHealthScenario = scenario("Actuator Health Check")
    .during(2.minutes) {
      exec(Health.actuatorHealthCheck)
    }

  val detailedHealthScenario = scenario("Detailed Health Check")
    .during(1.minute) {
      exec(Health.detailedActuatorHealthCheck)
    }

  setUp(
    basicHealthScenario.inject(rampUsers(5) during (10.seconds)),
    actuatorHealthScenario.inject(rampUsers(5) during (10.seconds)),
    detailedHealthScenario.inject(nothingFor(5.seconds), rampUsers(3) during (5.seconds))
  ).protocols(Health.httpProtocol)
   .maxDuration(2.minutes)
}

class LoadSimulationEntrypoint extends Simulation {

  val mixedHealthWorkload = scenario("Mixed Health Workload")
    .during(3.minutes) {
      exec(Health.mixedHealthChecks)
    }

  val rapidHealthChecks = scenario("Rapid Health Monitoring")
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

  val healthStressTest = scenario("Health Endpoint Stress Test")
    .during(5.minutes) {
      exec(Health.mixedHealthChecks)
    }

  val highFrequencyMonitoring = scenario("High-Frequency Health Monitoring")
    .during(4.minutes) {
      exec(Health.rapidHealthCheck)
    }

  val sequentialHealthTest = scenario("Sequential Health Check Test")
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