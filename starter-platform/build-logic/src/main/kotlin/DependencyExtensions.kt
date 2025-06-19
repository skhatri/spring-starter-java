import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.VersionCatalog

fun DependencyHandler.main(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.provided(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("compileOnly", dependency)
    }
}

fun DependencyHandler.test(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("testImplementation", dependency)
    }
}

fun DependencyHandler.testRuntime(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("testRuntimeOnly", dependency)
    }
}

fun DependencyHandler.providedTest(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("testCompileOnly", dependency)
    }
}

fun DependencyHandler.api(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("api", dependency)
    }
}

fun DependencyHandler.errorprone(vararg dependencies: Any) {
    dependencies.forEach { dependency ->
        add("errorprone", dependency)
    }
}

fun VersionCatalog.bundle(bundleName: String) = findBundle(bundleName).get()
fun VersionCatalog.library(libraryName: String) = findLibrary(libraryName).get()
fun VersionCatalog.version(versionName: String) = findVersion(versionName).get().toString() 