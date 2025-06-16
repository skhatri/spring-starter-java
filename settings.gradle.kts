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
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs/versions.toml"))
        }
    }
}
