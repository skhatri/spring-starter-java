plugins {
    id("starter.spring-conventions")
}

tasks.test {
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
    mainClass = "com.github.starter.math.Application"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms256m", "-Xmx256m")
} 