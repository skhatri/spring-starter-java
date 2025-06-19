package com.github.starter.poke.pokemon

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

object Pokemon {
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
    .userAgentHeader("Gatling-Pokemon-LoadTest/1.0")

  val httpProtocol = if (http2Enabled) {
    httpProtocolBase.enableHttp2.http2PriorKnowledge(Map(s"$server:$port" -> true))
  } else {
    httpProtocolBase
  }

  val pokemonFeeder = csv("find-pokemon.csv").random

  val findPokemon = feed(pokemonFeeder)
    .exec(http("Find Pokemon - #{pokemon}")
      .get("/pokemon/#{pokemon}")
      .check(status.is(200))
      .check(jsonPath("$.name").is("#{pokemon}"))
    )
    .pause(1, 3)

  val listFirstPage = exec(http("List Pokemon - First Page")
    .get("/pokemon/list")
    .queryParam("limit", "10")
    .check(status.is(200))
    .check(jsonPath("$.data").exists)
    .check(jsonPath("$.pagination.hasNext").saveAs("hasNext"))
    .check(jsonPath("$.pagination.nextToken").optional.saveAs("nextCursor"))
  )
  .pause(1, 2)

  val listWithPagination = exec(http("List Pokemon - First Page")
    .get("/pokemon/list")
    .queryParam("limit", "20")
    .check(status.is(200))
    .check(jsonPath("$.data").exists)
    .check(jsonPath("$.pagination.hasNext").saveAs("hasNext"))
    .check(jsonPath("$.pagination.nextToken").optional.saveAs("nextCursor"))
  )
  .pause(1, 2)
  .asLongAs(session => session("hasNext").as[Boolean]) {
    exec(http("List Pokemon - Next Page")
      .get("/pokemon/list")
      .queryParam("limit", "20")
      .queryParam("nextToken", "#{nextCursor}")
      .check(status.is(200))
      .check(jsonPath("$.data").exists)
      .check(jsonPath("$.pagination.hasNext").saveAs("hasNext"))
      .check(jsonPath("$.pagination.nextToken").optional.saveAs("nextCursor"))
    )
    .pause(1, 2)
  }

  val mixedBehavior = randomSwitch(
    50.0 -> exec(findPokemon),
    50.0 -> exec(listFirstPage)
  )
}

class SimulationEntrypoint extends Simulation {

  val findPokemonScenario = scenario("Find Pokemon by Name")
    .during(2.minutes) {
      exec(Pokemon.findPokemon)
    }

  val listPokemonScenario = scenario("List Pokemon - First Page Only")
    .during(2.minutes) {
      exec(Pokemon.listFirstPage)
    }

  val paginationScenario = scenario("List Pokemon - Full Pagination")
    .during(1.minute) {
      exec(Pokemon.listWithPagination)
    }

  setUp(
    findPokemonScenario.inject(rampUsers(10) during (15.seconds)),
    listPokemonScenario.inject(rampUsers(8) during (10.seconds)),
    paginationScenario.inject(nothingFor(10.seconds), rampUsers(3) during (5.seconds))
  ).protocols(Pokemon.httpProtocol)
   .maxDuration(2.minutes)
}

class LoadSimulationEntrypoint extends Simulation {

  val mixedWorkloadScenario = scenario("Mixed Pokemon Workload")
    .during(3.minutes) {
      exec(Pokemon.mixedBehavior)
    }

  setUp(
    mixedWorkloadScenario.inject(
      constantConcurrentUsers(5) during (30.seconds),
      rampConcurrentUsers(5) to (15) during (30.seconds),
      constantConcurrentUsers(15) during (1.minute),
      rampConcurrentUsers(15) to (5) during (30.seconds)
    )
  ).protocols(Pokemon.httpProtocol)
   .maxDuration(3.minutes)
}

class StressSimulationEntrypoint extends Simulation {

  val stressScenario = scenario("Pokemon API Stress Test")
    .during(5.minutes) {
      exec(Pokemon.mixedBehavior)
    }

  setUp(
    stressScenario.inject(
      rampConcurrentUsers(1) to (30) during (1.minute),
      constantConcurrentUsers(30) during (3.minutes),
      rampConcurrentUsers(30) to (5) during (1.minute)
    )
  ).protocols(Pokemon.httpProtocol)
   .maxDuration(5.minutes)
   .assertions(
     global.responseTime.max.lt(5000),
     global.responseTime.mean.lt(1000),
     global.successfulRequests.percent.gt(95)
   )
} 