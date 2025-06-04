plugins {
    java
}

group = "com.github"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.testing.junit)
    testImplementation(libs.bundles.testing.integration)
    testRuntimeOnly(libs.bundles.testing.runtime)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
        
        // Only run integration tests if explicitly specified with -Dtags="integration"
        if (project.hasProperty("tags") || project.hasProperty("includeTags")) {
            if (project.hasProperty("includeTags")) {
                includeTags(project.property("includeTags").toString())
            } else if (project.hasProperty("tags")) {
                includeTags(project.property("tags").toString())
            }
        } else {
            // No tags specified, exclude all tests
            excludeTags("integration")
        }
        
        if (project.hasProperty("excludeTags")) {
            excludeTags(project.property("excludeTags").toString())
        }
    }
    maxParallelForks = 1
    reports {
        html.required = true
        junitXml.required = true
    }
    ignoreFailures = false
}

// Create a dedicated integrationTest task that always runs integration tests
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    
    useJUnitPlatform {
        includeEngines("junit-jupiter")
        includeTags("integration")
    }
    
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    
    maxParallelForks = 1
    reports {
        html.required = true
        junitXml.required = true
    }
    ignoreFailures = false
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-XDcompilePolicy=byfile")
    options.compilerArgs.add("--should-stop=ifError=FLOW")
} 