rootProject.name="spring-starter-java"

listOf("app", "pokemon-common").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
}

