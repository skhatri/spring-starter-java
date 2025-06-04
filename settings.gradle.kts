rootProject.name="spring-starter-java"
listOf("app", "integration-test", "db-migration").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("net.ltgt.errorprone") version "3.1.0"
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs/versions.toml"))
        }
    }
}
