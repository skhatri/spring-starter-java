plugins {
    id("org.sonarqube") version "5.1.0.4882"
    id("jacoco")
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sonarqube {
    properties {
        property("sonar.projectName", "${project.name}")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.projectKey", "${rootProject.name}-${project.name}")
        property("sonar.projectVersion", "${project.version}")
        property("sonar.junit.reportPaths", "${projectDir}/build/test-results/test")
        property("sonar.coverage.jacoco.xmlReportPaths", "${projectDir}/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/R.java,**/proto/*.java")
    }
}

val jupiterVersion = "5.11.0"
val junitPlatformVersion = "1.11.0"

dependencies {
    listOf(
        "spring-boot-starter-webflux",
        "spring-boot-starter-reactor-netty",
        "spring-boot-starter",
        "spring-boot-starter-websocket",
        "spring-boot-starter-graphql",
    ).forEach { name ->
        implementation("org.springframework.boot:${name}") {
            exclude(module = "spring-boot-starter-logging")
        }
    }
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.17.2"))
    implementation("org.yaml:snakeyaml:2.2")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.postgresql:r2dbc-postgresql:1.0.5.RELEASE")
    implementation("org.duckdb:duckdb_jdbc:1.0.0")
    implementation("org.springframework.data:spring-data-r2dbc:3.3.3")
    implementation(project(":pokemon-common"))

    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.boot:spring-boot-test") {
        exclude(module="spring-boot-starter-logging")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module="spring-boot-starter-logging")
    }

    testImplementation("com.intuit.karate:karate-junit5:1.4.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")

    testImplementation("org.junit.platform:junit-platform-commons:$junitPlatformVersion")
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:$junitPlatformVersion")
}

task("runApp", JavaExec::class) {
    mainClass = "com.github.starter.Application"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
    environment("DATASET_DIR", "${projectDir}/../db")
}

tasks.test {
    useJUnitPlatform()
    environment("DATASET_DIR", "${projectDir}/../db")
}

configurations.all {
    exclude(group="ch.qos.logback", module="logback-classic")
}
