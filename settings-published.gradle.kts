rootProject.name="spring-starter-java"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
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

listOf("pokemon-service", "integration-test", "db-migration", "load-testing").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
} 