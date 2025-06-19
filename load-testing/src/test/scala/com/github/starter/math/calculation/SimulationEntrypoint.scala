package com.github.starter.math.calculation

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

object Calculation {
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
    .userAgentHeader("Gatling-Math-Calculation-LoadTest/1.0")

  val httpProtocol = if (http2Enabled) {
    httpProtocolBase.enableHttp2.http2PriorKnowledge(Map(s"$server:$port" -> true))
  } else {
    httpProtocolBase
  }

  val calculationFeeder = Iterator.continually(Map(
    "operand1" -> (scala.util.Random.nextDouble() * 100).toString,
    "operand2" -> (scala.util.Random.nextDouble() * 100).toString,
    "operation" -> scala.util.Random.shuffle(List("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE")).head
  ))

  val simpleCalculationFeeder = Iterator.continually(Map(
    "operand1" -> (scala.util.Random.nextInt(100) + 1).toString,
    "operand2" -> (scala.util.Random.nextInt(10) + 1).toString,
    "operation" -> scala.util.Random.shuffle(List("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE")).head
  ))

  val restCalculation = feed(calculationFeeder)
    .exec(http("REST Calculation - #{operation}")
      .post("/calculate")
      .body(StringBody("""{"number1": #{operand1}, "number2": #{operand2}, "operation": "#{operation}"}"""))
      .check(status.is(200))
      .check(jsonPath("$.result").exists)
      .check(jsonPath("$.expression").exists)
      .check(jsonPath("$.operation").is("#{operation}"))
    )
    .pause(500.milliseconds, 2.seconds)

  val graphqlCalculationQuery = feed(calculationFeeder)
    .exec(http("GraphQL Calculation Query - #{operation}")
      .post("/graphql")
      .body(StringBody("""{"query": "query { calculate(input: { operand1: #{operand1}, operand2: #{operand2}, operation: #{operation} }) { result expression operation } }"}"""))
      .check(status.is(200))
      .check(jsonPath("$.data.calculate.result").exists)
      .check(jsonPath("$.data.calculate.expression").exists)
      .check(jsonPath("$.data.calculate.operation").is("#{operation}"))
    )
    .pause(500.milliseconds, 2.seconds)

  val graphqlCalculationMutation = feed(calculationFeeder)
    .exec(http("GraphQL Calculation Mutation - #{operation}")
      .post("/graphql")
      .body(StringBody("""{"query": "mutation { performCalculation(input: { operand1: #{operand1}, operand2: #{operand2}, operation: #{operation} }) { id calculation { result expression operation } timestamp } }"}"""))
      .check(status.is(200))
      .check(jsonPath("$.data.performCalculation.id").exists)
      .check(jsonPath("$.data.performCalculation.calculation.result").exists)
      .check(jsonPath("$.data.performCalculation.timestamp").exists)
    )
    .pause(500.milliseconds, 2.seconds)

  val graphqlSupportedOperations = exec(http("GraphQL Supported Operations")
    .post("/graphql")
    .body(StringBody("""{"query": "query { supportedOperations }"}"""))
    .check(status.is(200))
    .check(jsonPath("$.data.supportedOperations").exists)
    .check(jsonPath("$.data.supportedOperations[*]").count.gte(4))
  )
  .pause(1.second, 3.seconds)

  val divisionByZeroTest = exec(http("Division by Zero Test")
    .post("/calculate")
    .body(StringBody("""{"number1": 10, "number2": 0, "operation": "DIVIDE"}"""))
    .check(status.is(400))
  )
  .pause(500.milliseconds, 1.second)

  val invalidOperationTest = exec(http("Invalid Operation Test")
    .post("/calculate")
    .body(StringBody("""{"number1": 10, "number2": 5, "operation": "INVALID"}"""))
    .check(status.is(400))
  )
  .pause(500.milliseconds, 1.second)

  val mixedCalculationBehavior = randomSwitch(
    30.0 -> exec(restCalculation),
    25.0 -> exec(graphqlCalculationQuery),
    20.0 -> exec(graphqlCalculationMutation),
    10.0 -> exec(graphqlSupportedOperations),
    10.0 -> exec(divisionByZeroTest),
    5.0 -> exec(invalidOperationTest)
  )

  val heavyMathWorkload = feed(simpleCalculationFeeder)
    .exec(http("Heavy Math Calculation - #{operation}")
      .post("/calculate")
      .body(StringBody("""{"number1": #{operand1}, "number2": #{operand2}, "operation": "#{operation}"}"""))
      .check(status.is(200))
      .check(jsonPath("$.result").exists)
    )
    .pause(100.milliseconds, 500.milliseconds)

  val sequentialOperationsTest = exec(restCalculation)
    .pause(200.milliseconds)
    .exec(graphqlCalculationQuery)
    .pause(200.milliseconds)
    .exec(graphqlCalculationMutation)
    .pause(500.milliseconds)
}

class SimulationEntrypoint extends Simulation {

  val restCalculationScenario = scenario("Math Service REST Calculations")
    .during(2.minutes) {
      exec(Calculation.restCalculation)
    }

  val graphqlCalculationScenario = scenario("Math Service GraphQL Calculations")
    .during(2.minutes) {
      exec(Calculation.graphqlCalculationQuery)
    }

  val graphqlMutationScenario = scenario("Math Service GraphQL Mutations")
    .during(1.minute) {
      exec(Calculation.graphqlCalculationMutation)
    }

  val errorHandlingScenario = scenario("Math Service Error Handling")
    .during(1.minute) {
      randomSwitch(
        50.0 -> exec(Calculation.divisionByZeroTest),
        50.0 -> exec(Calculation.invalidOperationTest)
      )
    }

  setUp(
    restCalculationScenario.inject(rampUsers(8) during (15.seconds)),
    graphqlCalculationScenario.inject(rampUsers(6) during (10.seconds)),
    graphqlMutationScenario.inject(nothingFor(10.seconds), rampUsers(4) during (10.seconds)),
    errorHandlingScenario.inject(nothingFor(15.seconds), rampUsers(2) during (5.seconds))
  ).protocols(Calculation.httpProtocol)
   .maxDuration(2.minutes)
}

class LoadSimulationEntrypoint extends Simulation {

  val mixedCalculationWorkload = scenario("Math Service Mixed Calculation Workload")
    .during(3.minutes) {
      exec(Calculation.mixedCalculationBehavior)
    }

  val heavyMathWorkload = scenario("Math Service Heavy Math Workload")
    .during(2.minutes) {
      exec(Calculation.heavyMathWorkload)
    }

  setUp(
    mixedCalculationWorkload.inject(
      rampUsers(10) during (30.seconds),
      constantUsersPerSec(15) during (1.minute),
      rampUsers(15) during (30.seconds)
    ),
    heavyMathWorkload.inject(
      nothingFor(30.seconds),
      rampUsers(20) during (30.seconds),
      constantUsersPerSec(25) during (1.minute)
    )
  ).protocols(Calculation.httpProtocol)
   .maxDuration(3.minutes)
}

class StressSimulationEntrypoint extends Simulation {

  val calculationStressTest = scenario("Math Service Calculation Stress Test")
    .during(5.minutes) {
      exec(Calculation.mixedCalculationBehavior)
    }

  val highThroughputCalculations = scenario("Math Service High-Throughput Calculations")
    .during(4.minutes) {
      exec(Calculation.heavyMathWorkload)
    }

  val sequentialOperationsStress = scenario("Math Service Sequential Operations Stress")
    .during(3.minutes) {
      exec(Calculation.sequentialOperationsTest)
    }

  setUp(
    calculationStressTest.inject(
      rampUsers(30) during (1.minute),
      constantUsersPerSec(40) during (2.minutes),
      rampUsers(20) during (1.minute)
    ),
    highThroughputCalculations.inject(
      nothingFor(30.seconds),
      rampUsers(50) during (1.minute),
      constantUsersPerSec(60) during (2.minutes)
    ),
    sequentialOperationsStress.inject(
      nothingFor(1.minute),
      rampUsers(15) during (30.seconds),
      constantUsersPerSec(20) during (2.minutes)
    )
  ).protocols(Calculation.httpProtocol)
   .maxDuration(5.minutes)
   .assertions(
     global.responseTime.max.lt(2000),
     global.responseTime.mean.lt(300),
     global.successfulRequests.percent.gt(95),
     forAll.failedRequests.percent.lt(5)
   )
} 