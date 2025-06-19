plugins {
    `java-platform`
    `maven-publish`
}

group = "com.github.starter"
version = "0.1.0"

dependencies {
    constraints {
        api("com.github.starter:starter-core:0.1.0")
        
        api(libs.spring.boot.starter.webflux)
        api(libs.spring.boot.starter.actuator)
        api(libs.spring.boot.starter.graphql)
        api(libs.spring.boot.starter.reactor.netty)
        api(libs.spring.boot.starter.websocket)
        api(libs.spring.boot.starter.log4j2)
        api(libs.spring.boot.starter.test)
        api(libs.spring.boot.test)
        
        api(libs.r2dbc.postgresql)
        api(libs.spring.data.r2dbc)
        api(libs.postgresql.jdbc)
        
        api(libs.junit.jupiter.api)
        api(libs.junit.jupiter.engine)
        api(libs.junit.jupiter.params)
        api(libs.junit.platform.commons)
        api(libs.junit.platform.runner)
        api(libs.junit.platform.launcher)
        api(libs.junit.platform.engine)
        api(libs.mockito.core)
        api(libs.mockito.junit.jupiter)
        api(libs.testcontainers)
        api(libs.testcontainers.junit.jupiter)
        api(libs.testcontainers.postgresql)
        api(libs.testcontainers.r2dbc)
        api(libs.karate.junit5)
        api(libs.reactor.test)
        
        api(libs.opentelemetry.api)
        api(libs.opentelemetry.sdk)
        api(libs.opentelemetry.context)
        api(libs.opentelemetry.exporter.otlp)
        api(libs.opentelemetry.instrumentation.annotations)
        api(libs.opentelemetry.spring.boot.starter)
        api(libs.micrometer.registry.prometheus)
        api(libs.micrometer.tracing.bridge.otel)
        
        api(libs.snakeyaml)
        
        api(libs.errorprone.core)
        api(libs.errorprone.annotations)
        api(libs.spotbugs.annotations)
        
        api(libs.scala.compiler)
        api(libs.scala.library)
        api(libs.gatling.core)
        api(libs.gatling.app)
        api(libs.gatling.http)
        api(libs.gatling.http.client)
        api(libs.gatling.charts)
        api(libs.gatling.charts.highcharts)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            
            pom {
                name.set("Microservices Starter")
                description.set("Starter Bill of Materials")
            }
        }
    }
} 