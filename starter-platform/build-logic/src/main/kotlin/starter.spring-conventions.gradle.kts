import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("starter.java-conventions")
    id("starter.platform-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    main(libs.bundle("spring-boot-core"))
    test(libs.bundle("spring-testing"))
    main(libs.bundle("otel"))
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

fun BootJar.configureBootJar() {
    archiveClassifier.set("uber")
    
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "GitHub Starter Platform"
        )
    }
}

fun Jar.configurePlainJar() {
    enabled = true
    archiveClassifier.set("plain")
    
    manifest {
        attributes(
            "Implementation-Title" to "${project.name}-library",
            "Implementation-Version" to project.version
        )
    }
}

tasks.bootJar {
    configureBootJar()
}

tasks.jar {
    configurePlainJar()
}

springBoot {
    buildInfo {
        properties {
            name = project.name
            version = project.version.toString()
            group = project.group.toString()
            artifact = project.name
        }
    }
} 