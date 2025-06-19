import com.github.spotbugs.snom.SpotBugsExtension

plugins {
    java
    jacoco
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    main(libs.bundle("quality-analysis"))
    errorprone(libs.library("errorprone-core"))
    test(libs.bundle("testing-unit"))
    testRuntime(libs.bundle("testing-runtime"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf(
        "--should-stop=ifError=FLOW"
    ))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

jacoco {
    toolVersion = libs.findVersion("jacoco").get().toString()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
    
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.spotbugsMain {
    reports.create("html") {
        required.set(true)
    }
    reports.create("xml") {
        required.set(true)
    }
} 
