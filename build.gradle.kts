buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("idea")
    id("java")
    id("net.ltgt.errorprone") apply false
    id("com.github.spotbugs") version "6.0.8" apply false
    alias(libs.plugins.dependency.check) apply false
}
allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
tasks.wrapper {
    gradleVersion = "8.7"
    distributionType = Wrapper.DistributionType.ALL
}
