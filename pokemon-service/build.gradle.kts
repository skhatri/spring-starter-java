plugins {
    id("starter.spring-conventions")
    id("starter.postgres-conventions")
}

spotbugs {
    excludeFilter.set(file("${rootProject.projectDir}/config/spotbugs-exclude.xml"))
}

tasks.test {
    environment("DATASET_DIR", "${projectDir}/../scripts/containers/postgres/csv")
    environment("APP_DB", "pg")
    environment("NO_WIREMOCK", project.findProperty("no.wiremock")?.let { it.toString() } ?: "true")
    environment("NO_BOOT", project.findProperty("no.boot")?.let { it.toString() } ?: "true")
    maxParallelForks = 1
    ignoreFailures = false
}

tasks.jar {
    enabled = false
    archiveClassifier = "plain"
}

tasks.build {
    dependsOn(tasks.bootJar)
}

task("runApp", JavaExec::class) {
    mainClass = "com.github.starter.Application"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx512m")
    environment("DATASET_DIR", "${projectDir}/../db")
    environment("APP_DB", "pg")
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
            content = content.replace("/tmp/data/csv/", "/docker-entrypoint-initdb.d/csv/")
            outputFile.appendText(content)
            outputFile.appendText("\n\n")
        }
        
        logger.lifecycle("Generated init-combined.sql from ${migrationFiles.size + 1} files")
    }
}
