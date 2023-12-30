import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("jacoco")
    kotlin("plugin.spring") version "1.8.10"
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

