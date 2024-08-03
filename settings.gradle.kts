rootProject.name="spring-starter"

listOf("app", "pokemon-common").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
}

