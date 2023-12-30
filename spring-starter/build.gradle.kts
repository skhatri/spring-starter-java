import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.sonarqube") version "3.5.0.2730"
    id("jacoco")
    kotlin("plugin.spring") version "1.8.10"
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
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


dependencies {
    listOf(
        "spring-boot-starter-webflux",
        "spring-boot-starter-reactor-netty",
        "spring-boot-starter"
    ).forEach { name ->
        implementation("org.springframework.boot:${name}") {
            exclude(module = "spring-boot-starter-logging")
        }
    }
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.14.2"))
    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    implementation("org.springframework.data:spring-data-r2dbc:3.0.4")
    implementation(project(":pokemon-common"))
}

task("runApp", JavaExec::class) {
    main = "com.github.starter.ApplicationKt"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
}

