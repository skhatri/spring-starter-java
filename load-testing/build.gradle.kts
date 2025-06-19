plugins {
    id("scala")
}

dependencies {
    implementation(libs.bundles.scala)
    implementation(libs.bundles.gatling)
}

fun createGatlingTask(taskName: String, simulationClass: String, enableReports: Boolean = false): TaskProvider<JavaExec> {
    return tasks.register(taskName, JavaExec::class) {
        mainClass = "io.gatling.app.Gatling"
        classpath = sourceSets["test"].runtimeClasspath
        
        val baseArgs = mutableListOf(
            "-bf", "${sourceSets["test"].output.classesDirs.asPath}",
            "-rsf", "${sourceSets["test"].resources.srcDirs.first()}",
            "-rf", "$projectDir/build/reports/gatling"
        )
        
        if (!enableReports) {
            baseArgs.add("-nr")
        }
        
        baseArgs.addAll(listOf("-s", simulationClass))
        args = baseArgs
        
        jvmArgs = listOf(
            "-Xms512m", "-Xmx1024m", "-XX:+UseZGC",
            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens=java.base/java.nio=ALL-UNNAMED",
            "--add-exports=java.base/sun.util.calendar=ALL-UNNAMED"
        )
        
        group = "load-testing"
        description = "Run $simulationClass Gatling simulation"
    }
}

val testModule = project.findProperty("testModule") ?: "pokemon"
val appName = project.findProperty("appName") ?: "poke"
val basePackage = "com.github.starter.$appName.$testModule"

val gatlingSimulations = mapOf(
    "runTest" to "${basePackage}.SimulationEntrypoint",
    "runLoadTest" to "${basePackage}.LoadSimulationEntrypoint", 
    "runStressTest" to "${basePackage}.StressSimulationEntrypoint"
)

gatlingSimulations.forEach { (taskName, simulationClass) ->
    createGatlingTask(taskName, simulationClass, true)
}


scala {
}

tasks.withType<ScalaCompile>().configureEach {
    scalaCompileOptions.forkOptions.apply {
        memoryMaximumSize = "1g"
        jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
    }
    scalaCompileOptions.additionalParameters = listOf("-language:postfixOps")
}
