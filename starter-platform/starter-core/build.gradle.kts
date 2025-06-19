plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.starter"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.0"))
    api(platform(libs.jackson.bom))
    api(platform(libs.opentelemetry.instrumentation.bom))
    
    api(libs.bundles.spring.boot.core) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api(libs.spring.boot.starter.actuator) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api(libs.bundles.spring.boot.logging)
    api(libs.bundles.database)
    api(libs.bundles.observability) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    
    api(libs.snakeyaml)
    
    testImplementation(libs.bundles.testing.junit)
    testImplementation(libs.bundles.testing.mockito)
    testImplementation(libs.bundles.spring.testing) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    testImplementation(libs.reactor.test)
    
    testRuntimeOnly(libs.bundles.testing.runtime)
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf(
        "-Xlint:unchecked",
        "-Xlint:deprecation"
    ))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Starter Core")
                description.set("Core shared components for starter platform")
                
                developers {
                    developer {
                        id.set("starter-team")
                        name.set("Starter Platform Team")
                    }
                }
            }
        }
    }
} 