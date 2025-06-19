rootProject.name="spring-starter-java"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    includeBuild("starter-platform/build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs/versions.toml"))
        }
    }
}

includeBuild("starter-platform")

listOf("pokemon-service", "math-service", "integration-test", "db-migration", "load-testing").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
} 