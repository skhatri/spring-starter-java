buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("idea")
    id("java")
    alias(libs.plugins.errorprone) apply false
    alias(libs.plugins.spotbugs) apply false
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
    gradleVersion = libs.versions.gradle.wrapper.get()
    distributionType = Wrapper.DistributionType.ALL
}
