pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs/versions.toml"))
        }
    }
}

rootProject.name = "starter-platform"

includeBuild("build-logic")
include("starter-platform-bom")
include("starter-core") 