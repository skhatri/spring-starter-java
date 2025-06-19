plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    implementation(gradleApi())
    implementation(libs.findLibrary("spring-boot-gradle-plugin").get())
    implementation(libs.findLibrary("spring-dependency-management-plugin").get())
    implementation(libs.findLibrary("spotbugs-plugin").get())
    implementation(libs.findLibrary("errorprone-plugin").get())
    implementation(libs.findLibrary("foojay-resolver").get())
} 