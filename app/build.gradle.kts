plugins {
    alias(libs.plugins.jacoco)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.dependency.check)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = false
        html.required = true
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/html")
    }
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
            enabled = true
            element = "CLASS"
            includes = listOf("com.github.starter.*")
            excludes = listOf(
                "com.github.starter.Application",
                "**/*Config*",
                "**/*Exception*"
            )
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
        if (project.hasProperty("includeTags")) {
            includeTags(project.property("includeTags").toString())
        }
        if (project.hasProperty("excludeTags")) {
            excludeTags(project.property("excludeTags").toString())
        }
    }
    environment("DATASET_DIR", "${projectDir}/../scripts/containers/postgres/csv")
    environment("APP_DB", "pg")
    environment("NO_WIREMOCK", project.findProperty("no.wiremock")?.let { it.toString() } ?: "true")
    environment("NO_BOOT", project.findProperty("no.boot")?.let { it.toString() } ?: "true")
    maxParallelForks = 1
    reports {
        html.required = true
        junitXml.required = true
    }
    ignoreFailures = false
    finalizedBy(tasks.jacocoTestReport)
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-XDcompilePolicy=byfile")
    options.compilerArgs.add("--should-stop=ifError=FLOW")
}
dependencies {
    implementation(platform(libs.opentelemetry.instrumentation.bom))
    implementation(libs.bundles.spring.boot.core) {
        exclude(module = "spring-boot-starter-logging")
    }
    implementation(libs.spring.boot.starter.actuator) {
        exclude(module = "spring-boot-starter-logging")
    }
    implementation(libs.spring.boot.starter.log4j2)
    implementation(platform(libs.jackson.bom))
    implementation(libs.snakeyaml)
    implementation(libs.bundles.database)
    runtimeOnly(libs.postgresql.jdbc)
    implementation(libs.bundles.observability)
    implementation(libs.spotbugs.annotations)
    
    testImplementation(libs.bundles.spring.testing) {
        exclude(module = "spring-boot-starter-logging")
    }
    testImplementation(libs.bundles.testing.junit)
    testImplementation(libs.bundles.testing.integration)
    testImplementation(libs.bundles.testing.mockito)
    testImplementation(libs.reactor.test)
    testRuntimeOnly(libs.bundles.testing.runtime)
    
    errorprone(libs.errorprone.core)
    compileOnly(libs.errorprone.annotations)
}
task("runApp", JavaExec::class) {
    mainClass = "com.github.starter.Application"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
    environment("DATASET_DIR", "${projectDir}/../db")
    environment("APP_DB", "pg")
}
configurations.all {
    exclude(group="ch.qos.logback", module="logback-classic")
}
spotbugs {
    ignoreFailures = true
    excludeFilter.set(file("${rootProject.projectDir}/config/spotbugs-exclude.xml"))
}
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    dependsOn(tasks.named("generateInitCombined"))
    
    reports {
        create("html") {
            required.set(true)
        }
        create("xml") {
            required.set(true)
        }
    }
    doLast {
        val htmlReportFile = layout.buildDirectory.file("reports/spotbugs/${name.replace("spotbugs", "").lowercase()}.html").get().asFile
        val xmlReportFile = layout.buildDirectory.file("reports/spotbugs/${name.replace("spotbugs", "").lowercase()}.xml").get().asFile
        if (htmlReportFile.exists()) {
            logger.lifecycle("SpotBugs HTML Report: file://${htmlReportFile.absolutePath}")
        }
        if (xmlReportFile.exists()) {
            try {
                val xmlContent = xmlReportFile.readText()
                val issueCount = xmlContent.split("<BugInstance").size - 1
                if (issueCount > 0) {
                    logger.lifecycle("Found $issueCount potential issues in ${name}")
                } else {
                    logger.lifecycle("No issues found in ${name}")
                }
            } catch (e: Exception) {
                logger.warn("Could not parse SpotBugs XML report: ${e.message}")
            }
        }
    }
}
dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFile = "${rootProject.projectDir}/config/dependency-check-suppressions.xml"
    format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL.toString()
    nvd.apiKey = project.findProperty("dependency.nvd.apiKey")?.toString()
}

tasks.register("generateInitCombined") {
    description = "Generates init-combined.sql from db-migration files for testcontainers"
    group = "build"
    
    val dbMigrationDir = file("${rootProject.projectDir}/db-migration")
    val userSetupFile = dbMigrationDir.resolve("2-db-user-setup.sql")
    val migrationDir = dbMigrationDir.resolve("src/main/database/pg")
    val outputFile = file("src/test/resources/db/init-combined.sql")
    
    inputs.file(userSetupFile)
    inputs.files(fileTree(migrationDir) { include("V*.sql") })
    outputs.file(outputFile)
    
    doLast {
        outputFile.parentFile.mkdirs()
        
        outputFile.writeText("")
        
        if (userSetupFile.exists()) {
            outputFile.appendText(userSetupFile.readText())
            outputFile.appendText("\n\n")
        }
        
        val migrationFiles = fileTree(migrationDir) { 
            include("V*.sql") 
        }.files.sortedBy { it.name }
        
        migrationFiles.forEach { file ->
            var content = file.readText()
            // Fix CSV paths for testcontainers
            content = content.replace("/tmp/data/csv/", "/docker-entrypoint-initdb.d/csv/")
            outputFile.appendText(content)
            outputFile.appendText("\n\n")
        }
        
        logger.lifecycle("Generated init-combined.sql from ${migrationFiles.size + 1} files")
    }
}

tasks.named("processTestResources") {
    finalizedBy("generateInitCombined")
}
